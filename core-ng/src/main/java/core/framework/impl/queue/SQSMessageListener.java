package core.framework.impl.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author neo
 */
public class SQSMessageListener implements Runnable, MessageHandlerConfig {
    private final Logger logger = LoggerFactory.getLogger(SQSMessageListener.class);

    // to keep backward compatible with core 2.x, SQS/SNS message will be replaced by RabbitMQ
    static final String MESSAGE_ATTR_PUBLISHER = "event_publisher";
    static final String MESSAGE_ATTR_TYPE = "event_type";
    static final String MESSAGE_ATTR_SENDER = "event_sender";
    static final String MESSAGE_ATTR_REQUEST_ID = "request_id";
    static final String MESSAGE_ATTR_TRACE = "trace";

    final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    final Executor executor;
    final AmazonSQS sqs;
    final String queueURL;

    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, Class> messageClasses = Maps.newHashMap();
    private final MessageHandlerCounter counter = new MessageHandlerCounter();
    private final MessageValidator validator;

    volatile boolean shutdown;

    public SQSMessageListener(Executor executor, AmazonSQS sqs, String queueURL, MessageValidator validator) {
        this.executor = executor;
        this.sqs = sqs;
        this.queueURL = queueURL;
        this.validator = validator;
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

        while (!shutdown) {
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

    public void shutdown() {
        logger.info("shutdown sqs message listener, queueURL={}", queueURL);
        shutdown = true;
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
                        logger.debug("delete message, handle={}", message.getReceiptHandle());
                        sqs.deleteMessage(queueURL, message.getReceiptHandle());
                        counter.decrease();
                    }
                });
            }
        }
    }

    private <T> void process(Message sqsMessage) throws Exception {
        logger.debug("queueURL={}", queueURL);
        logger.debug("message={}", sqsMessage);
        String messageType;
        String messageBody;
        String messageId;
        Map<String, MessageAttributeValue> attributes = sqsMessage.getMessageAttributes();
        MessageAttributeValue publisher = attributes.get(MESSAGE_ATTR_PUBLISHER);
        if (publisher != null && "sqs".equals(publisher.getStringValue())) {
            messageType = attributes.get(MESSAGE_ATTR_TYPE).getStringValue();
            messageBody = sqsMessage.getBody();
            messageId = sqsMessage.getMessageId();

            linkSQSContext(attributes, messageId);

            MessageAttributeValue sender = attributes.get(MESSAGE_ATTR_SENDER);
            if (sender != null) {
                ActionLogContext.put("sender", sender.getStringValue());
            }
        } else {
            // assume to be SNS
            SNSMessage snsMessage = JSON.fromJSON(SNSMessage.class, sqsMessage.getBody());
            messageType = snsMessage.subject;
            messageBody = snsMessage.message;
            messageId = snsMessage.messageId;

            linkSNSContext(snsMessage, messageId);

            if (snsMessage.attributes.eventSender != null) {
                ActionLogContext.put("sender", snsMessage.attributes.eventSender.value);
            }
        }
        ActionLogContext.put("messageId", messageId);

        if (Strings.empty(messageType)) throw new Error("messageType must not be empty");
        ActionLogContext.put(ActionLogContext.ACTION, "queue/" + messageType);

        ActionLogContext.get(ActionLogContext.TRACE).ifPresent(trace -> {
            if ("true".equals(trace))
                logger.warn("trace log is triggered for current message, messageId={}", messageId);
        });

        Class<T> messageClass = messageClass(messageType);
        T message = JSON.fromJSON(messageClass, messageBody);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        ActionLogContext.put("handler", handler.getClass().getCanonicalName());
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

    private void linkSNSContext(SNSMessage snsMessage, String messageId) {
        String requestId = snsMessage.attributes.requestId != null ? snsMessage.attributes.requestId.value : messageId;
        ActionLogContext.put(ActionLogContext.REQUEST_ID, requestId);

        if (snsMessage.attributes.trace != null && "true".equals(snsMessage.attributes.trace.value)) {
            ActionLogContext.put(ActionLogContext.TRACE, Boolean.TRUE);
        }
    }

    private void linkSQSContext(Map<String, MessageAttributeValue> attributes, String messageId) {
        MessageAttributeValue requestId = attributes.get(MESSAGE_ATTR_REQUEST_ID);
        ActionLogContext.put(ActionLogContext.REQUEST_ID, requestId != null ? requestId.getStringValue() : messageId);

        MessageAttributeValue trace = attributes.get(MESSAGE_ATTR_TRACE);
        if (trace != null && "true".equals(trace.getStringValue())) {
            ActionLogContext.put(ActionLogContext.TRACE, Boolean.TRUE);
        }
    }

    private List<Message> longPollFromSQS() {
        ReceiveMessageResult result = sqs.receiveMessage(new ReceiveMessageRequest(queueURL)
            .withWaitTimeSeconds(20)
            .withMaxNumberOfMessages(10)
            .withMessageAttributeNames(MESSAGE_ATTR_PUBLISHER,
                MESSAGE_ATTR_TYPE,
                MESSAGE_ATTR_SENDER,
                MESSAGE_ATTR_REQUEST_ID,
                MESSAGE_ATTR_TRACE));

        List<Message> messages = result.getMessages();
        logger.info("long poll from sqs, queueURL={}, receivedMessages={}", queueURL, messages.size());
        return messages;
    }
}
