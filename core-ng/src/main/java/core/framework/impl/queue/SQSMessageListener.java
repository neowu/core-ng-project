package core.framework.impl.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class SQSMessageListener implements Runnable, MessageHandlerConfig {
    private static final Pattern QUEUE_URL_PATTERN = Pattern.compile("https\\://sqs\\.([\\w-]+)\\.amazonaws\\.com(\\.cn){0,1}/(\\d+)/([\\w-]+)");
    private final Logger logger = LoggerFactory.getLogger(SQSMessageListener.class);

    // to keep backward compatible with core 2.x, SQS/SNS message will be replaced by RabbitMQ
    static final String MESSAGE_ATTR_PUBLISHER = "event_publisher";
    static final String MESSAGE_ATTR_TYPE = "event_type";

    static final String MESSAGE_ATTR_CLIENT_IP = "clientIP";
    static final String MESSAGE_ATTR_REF_ID = "ref-id";
    static final String MESSAGE_ATTR_TRACE = "trace";

    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private final Executor executor;
    private final AmazonSQS sqs;
    private final String queueURL;
    private final LogManager logManager;
    private final String queueName;

    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, Class> messageClasses = Maps.newHashMap();
    private final MessageHandlerCounter counter = new MessageHandlerCounter();
    private final MessageValidator validator;

    public SQSMessageListener(Executor executor, AmazonSQS sqs, String queueURL, MessageValidator validator, LogManager logManager) {
        this.executor = executor;
        this.sqs = sqs;
        this.queueURL = queueURL;
        this.validator = validator;
        this.logManager = logManager;
        Matcher matcher = QUEUE_URL_PATTERN.matcher(queueURL);
        if (!matcher.matches()) throw Exceptions.error("queue url does not match the pattern, queueURL={}", queueURL);
        queueName = matcher.group(4);
    }

    @Override
    public <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler) {
        if (handler.getClass().isSynthetic())
            throw Exceptions.error("handler class must not be anonymous or lambda, please create static class, handlerClass={}", handler.getClass().getCanonicalName());

        validator.register(messageClass);
        String messageType = messageClass.getDeclaredAnnotation(core.framework.api.queue.Message.class).name();
        messageClasses.put(messageType, messageClass);
        handlers.put(messageType, handler);
        return this;
    }

    @Override
    public SQSMessageListener maxConcurrentHandlers(int maxConcurrentHandlers) {
        counter.maxConcurrentHandlers = maxConcurrentHandlers;
        return this;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("sqs-listener-" + Thread.currentThread().getId());
        logger.info("sqs message listener started, queueURL={}", queueURL);

        while (!listenerExecutor.isShutdown()) {
            try {
                execute();
            } catch (Throwable e) {
                logger.error("failed to pull message, retry in 30 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(30));
            }
        }
    }

    public void start() {
        listenerExecutor.submit(this);
    }

    public void stop() {
        logger.info("stop sqs message listener, queueURL={}", queueURL);
        listenerExecutor.shutdown();
    }

    private void execute() throws InterruptedException {
        counter.waitUntilAvailable();
        List<Message> messages = longPollFromSQS();
        if (messages.isEmpty()) {
            Threads.sleepRoughly(Duration.ofSeconds(20));
        } else {
            for (Message message : messages) {
                counter.increase();
                executor.submit(() -> {
                    try {
                        process(message);
                        return null;
                    } finally {
                        counter.decrease();
                        logger.debug("delete message, handle={}", message.getReceiptHandle());
                        sqs.deleteMessage(queueURL, message.getReceiptHandle());
                    }
                });
            }
        }
    }

    private <T> void process(Message sqsMessage) throws Exception {
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("queue/" + queueName);

        logger.debug("queueURL={}", queueURL);
        logger.debug("message={}", sqsMessage);
        String messageType;
        String messageBody;
        Map<String, MessageAttributeValue> attributes = sqsMessage.getMessageAttributes();
        MessageAttributeValue publisher = attributes.get(MESSAGE_ATTR_PUBLISHER);
        if (publisher != null && "sqs".equals(publisher.getStringValue())) {
            messageType = attributes.get(MESSAGE_ATTR_TYPE).getStringValue();
            messageBody = sqsMessage.getBody();
            linkSQSContext(actionLog, attributes);

        } else {
            // assume to be SNS
            SNSMessage snsMessage = JSON.fromJSON(SNSMessage.class, sqsMessage.getBody());
            messageType = snsMessage.subject;
            messageBody = snsMessage.message;

            linkSNSContext(actionLog, snsMessage);

        }
        actionLog.context("messageType", messageType);
        if (Strings.empty(messageType)) throw new Error("messageType must not be empty");

        Class<T> messageClass = messageClass(messageType);
        T message = JSON.fromJSON(messageClass, messageBody);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        actionLog.context("handler", handler.getClass().getCanonicalName());
        handler.handle(message);
    }

    private <T> Class<T> messageClass(String messageType) {
        @SuppressWarnings("unchecked")
        Class<T> messageClass = messageClasses.get(messageType);
        if (messageClass == null) {
            throw Exceptions.error("can not find message class, messageType={}", messageType);
        }
        return messageClass;
    }

    private void linkSNSContext(ActionLog actionLog, SNSMessage snsMessage) {
        if (snsMessage.attributes.refId != null) {
            actionLog.refId(snsMessage.attributes.refId.value);
        }
        if (snsMessage.attributes.trace != null && "true".equals(snsMessage.attributes.trace.value)) {
            actionLog.triggerTraceLog();
        }
        if (snsMessage.attributes.clientIP != null) {
            actionLog.context("clientIP", snsMessage.attributes.clientIP.value);
        }
    }

    private void linkSQSContext(ActionLog actionLog, Map<String, MessageAttributeValue> attributes) {
        MessageAttributeValue refId = attributes.get(MESSAGE_ATTR_REF_ID);
        if (refId != null) actionLog.refId(refId.getStringValue());

        MessageAttributeValue trace = attributes.get(MESSAGE_ATTR_TRACE);
        if (trace != null && "true".equals(trace.getStringValue())) {
            actionLog.triggerTraceLog();
        }
        MessageAttributeValue clientIP = attributes.get(MESSAGE_ATTR_CLIENT_IP);
        if (clientIP != null) {
            actionLog.context("clientIP", clientIP.getStringValue());
        }
    }

    private List<Message> longPollFromSQS() {
        ReceiveMessageResult result = sqs.receiveMessage(new ReceiveMessageRequest(queueURL)
            .withWaitTimeSeconds(20)
            .withMaxNumberOfMessages(10)
            .withMessageAttributeNames(MESSAGE_ATTR_PUBLISHER,
                MESSAGE_ATTR_TYPE,
                MESSAGE_ATTR_CLIENT_IP,
                MESSAGE_ATTR_REF_ID,
                MESSAGE_ATTR_TRACE));

        List<Message> messages = result.getMessages();
        logger.info("long poll from sqs, queueURL={}, receivedMessages={}", queueURL, messages.size());
        return messages;
    }
}
