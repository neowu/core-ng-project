package core.framework.impl.queue;

import com.rabbitmq.client.QueueingConsumer;
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
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author neo
 */
public class RabbitMQListener implements Runnable, MessageHandlerConfig {
    static final String HEADER_TRACE = "trace";
    static final String HEADER_CLIENT_IP = "clientIP";

    private final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private final Executor executor;
    private final RabbitMQ rabbitMQ;
    private final LogManager logManager;
    private final String queue;

    private int maxConcurrentHandlers = 10;
    private Semaphore semaphore;
    private final MessageValidator validator;
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, Class> messageClasses = Maps.newHashMap();

    public RabbitMQListener(RabbitMQ rabbitMQ, String queue, Executor executor, MessageValidator validator, LogManager logManager) {
        this.executor = executor;
        this.rabbitMQ = rabbitMQ;
        this.queue = queue;
        this.validator = validator;
        this.logManager = logManager;
    }

    @Override
    public MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers) {
        this.maxConcurrentHandlers = maxConcurrentHandlers;
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
        semaphore = new Semaphore(maxConcurrentHandlers, false);
        while (!listenerExecutor.isShutdown()) {
            try (RabbitMQConsumer consumer = rabbitMQ.consumer(queue, maxConcurrentHandlers)) {
                pullMessage(consumer);
            } catch (Throwable e) {
                logger.error("failed to pull message, retry in 30 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(30));
            }
        }
    }

    private void pullMessage(RabbitMQConsumer consumer) {
        while (!listenerExecutor.isShutdown()) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            semaphore.acquireUninterruptibly(); // acquire permit right before submit, to avoid permit failing to release back due to exception in between
            executor.submit(() -> {
                try {
                    process(delivery);
                    return null;
                } finally {
                    semaphore.release(); // release permit first, not let exception from basic ack bypass it.
                    consumer.acknowledge(delivery.getEnvelope().getDeliveryTag());
                }
            });
        }
    }

    public void start() {
        listenerExecutor.submit(this);
    }

    public void shutdown() {
        logger.info("shutdown rabbitMQ message listener, queue={}", queue);
        listenerExecutor.shutdown();
    }

    private <T> void process(QueueingConsumer.Delivery delivery) throws Exception {
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("queue/" + queue);

        String messageBody = new String(delivery.getBody(), Charsets.UTF_8);
        String messageType = delivery.getProperties().getType();
        actionLog.context("messageType", messageType);

        logger.debug("message={}", messageBody);

        if (Strings.empty(messageType)) throw new Error("messageType must not be empty");

        actionLog.refId(delivery.getProperties().getCorrelationId());

        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers != null) {
            if ("true".equals(String.valueOf(headers.get(HEADER_TRACE)))) {
                actionLog.triggerTraceLog();
            }

            Object clientIP = headers.get(HEADER_CLIENT_IP);
            if (clientIP != null) {
                actionLog.context("clientIP", clientIP);
            }
        }

        String appId = delivery.getProperties().getAppId();
        if (appId != null) {
            actionLog.context("client", appId);
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
        actionLog.context("handler", handler.getClass().getCanonicalName());
        handler.handle(message);
    }
}
