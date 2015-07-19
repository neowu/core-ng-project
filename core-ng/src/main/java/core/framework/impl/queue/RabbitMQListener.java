package core.framework.impl.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import core.framework.api.log.ActionLogContext;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author neo
 */
public class RabbitMQListener implements Runnable, MessageHandlerConfig {
    static final String HEADER_REQUEST_ID = "requestId";
    static final String HEADER_TRACE = "trace";

    private final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    final Executor executor;
    final RabbitMQ rabbitMQ;
    final String queue;

    private final MessageHandlerCounter counter = new MessageHandlerCounter();
    private final MessageValidator validator;
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, Class> messageClasses = Maps.newHashMap();

    volatile boolean shutdown;

    public RabbitMQListener(RabbitMQ rabbitMQ, String queue, Executor executor, MessageValidator validator) {
        this.executor = executor;
        this.rabbitMQ = rabbitMQ;
        this.queue = queue;
        this.validator = validator;
    }

    @Override
    public MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers) {
        counter.maxConcurrentHandlers = maxConcurrentHandlers;
        return this;
    }

    @Override
    public <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler) {
        if (handler.getClass().isSynthetic())
            throw Exceptions.error("handler class must not be anonymous or lambda, please create static class, handlerClass={}", handler.getClass().getCanonicalName());

        validator.register(messageClass);
        String messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        messageClasses.put(messageType, messageClass);
        handlers.put(messageType, handler);
        return this;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("rabbitMQ-listener-" + Thread.currentThread().getId());
        logger.info("rabbitMQ message listener started, queue={}", queue);

        while (!shutdown) {
            try {
                execute();
            } catch (Throwable e) {
                logger.error("failed to pull message, retry in 30 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(30));
            }
        }
    }

    private void execute() throws InterruptedException, IOException {
        Channel channel = null;
        try {
            channel = rabbitMQ.channel();
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicQos(counter.maxConcurrentHandlers);
            channel.basicConsume(queue, false, consumer);
            consumeMessage(consumer);
        } finally {
            rabbitMQ.closeChannel(channel);
        }
    }

    private void consumeMessage(QueueingConsumer consumer) throws InterruptedException {
        while (!shutdown) {
            counter.waitUntilAvailable();
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            counter.increase();
            executor.submit(() -> {
                try {
                    process(delivery);
                    return null;
                } finally {
                    counter.decrease(); // release counter first, not let exception from basic ack bypass it.
                    //TODO: handle connection failure, reopen conn?
                    consumer.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            });
        }
    }

    public void start() {
        listenerExecutor.submit(this);
    }

    public void shutdown() {
        logger.info("shutdown rabbitMQ message listener, queue={}", queue);
        shutdown = true;
        listenerExecutor.shutdown();
    }

    private <T> void process(QueueingConsumer.Delivery delivery) throws Exception {
        String messageBody = new String(delivery.getBody(), Charsets.UTF_8);
        String messageType = delivery.getProperties().getType();
        logger.debug("messageType={}", messageType);
        logger.debug("message={}", messageBody);

        if (Strings.empty(messageType)) throw new Error("messageType must not be empty");
        ActionLogContext.put(ActionLogContext.ACTION, "queue/" + queue);

        String messageId = delivery.getProperties().getMessageId();
        ActionLogContext.put("messageId", messageId);

        Map<String, Object> headers = delivery.getProperties().getHeaders();
        linkContext(headers, messageId);

        Object sender = headers.get("sender");
        if (sender != null) {
            ActionLogContext.put("sender", sender);
        }

        @SuppressWarnings("unchecked")
        Class<T> messageClass = messageClasses.get(messageType);
        if (messageClass == null) {
            throw Exceptions.error("can not find message class, messageType={}", messageType);
        }
        T message = JSON.fromJSON(messageClass, messageBody);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        ActionLogContext.put("handler", handler.getClass().getCanonicalName());
        handler.handle(message);
    }

    private void linkContext(Map<String, Object> headers, String messageId) {
        Object requestId = headers.get(HEADER_REQUEST_ID);
        ActionLogContext.put(ActionLogContext.REQUEST_ID, requestId != null ? requestId : messageId);

        Object trace = headers.get(HEADER_TRACE);
        if ("true".equals(String.valueOf(trace))) {
            logger.warn("trace log is triggered for current message, messageId={}", messageId);
            ActionLogContext.put(ActionLogContext.TRACE, Boolean.TRUE);
        }
    }
}
