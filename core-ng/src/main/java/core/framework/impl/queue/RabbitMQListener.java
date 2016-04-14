package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.async.ThreadPools;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.LogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class RabbitMQListener implements MessageHandlerConfig {
    static final String HEADER_TRACE = "trace";
    static final String HEADER_CLIENT_IP = "clientIP";

    private final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread listenerThread;
    private final String queue;
    private final LogManager logManager;
    private final MessageValidator validator;
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, JSONReader> readers = Maps.newHashMap();
    private int poolSize = Runtime.getRuntime().availableProcessors() * 2;
    private ExecutorService handlerExecutor;

    public RabbitMQListener(RabbitMQ rabbitMQ, String queue, MessageValidator validator, LogManager logManager) {
        this.queue = queue;
        this.validator = validator;
        this.logManager = logManager;

        listenerThread = new Thread(() -> {
            logger.info("rabbitMQ listener started, queue={}", queue);
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = rabbitMQ.consumer(queue, poolSize * 2)) { // prefetch one more for each handler to improve throughput
                    while (!stop.get()) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        handlerExecutor.submit(() -> handle(consumer, delivery));
                    }
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to pull message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        }, "rabbitMQ-" + queue + "-listener");
    }

    @Override
    public <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler) {
        if (handler.getClass().isSynthetic())
            throw Exceptions.error("handler class must not be anonymous or lambda, please create static class, handlerClass={}", handler.getClass().getCanonicalName());

        validator.register(messageClass);
        String messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        readers.put(messageType, JSONReader.of(messageClass));
        handlers.put(messageType, handler);
        return this;
    }

    @Override
    public MessageHandlerConfig poolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public void start() {
        handlerExecutor = ThreadPools.cachedThreadPool(poolSize, "rabbitMQ-" + queue + "-handler-");
        listenerThread.start();
    }

    public void stop() {
        logger.info("stop rabbitMQ listener, queue={}", queue);
        stop.set(true);
        listenerThread.interrupt();
        handlerExecutor.shutdown();
        try {
            handlerExecutor.awaitTermination(10, TimeUnit.SECONDS);     // wait 10 seconds to finish current tasks
        } catch (InterruptedException e) {
            logger.warn("failed to wait all tasks to finish", e);
        }
    }

    private Void handle(RabbitMQConsumer consumer, QueueingConsumer.Delivery delivery) throws Exception {
        try {
            logManager.begin("=== message handling begin ===");
            handle(delivery);
            return null;
        } catch (Throwable e) {
            logManager.logError(e);
            throw e;
        } finally {
            acknowledge(consumer, delivery);
            logManager.end("=== message handling end ===");
        }
    }

    private void acknowledge(RabbitMQConsumer consumer, QueueingConsumer.Delivery delivery) {
        try {
            consumer.acknowledge(delivery.getEnvelope().getDeliveryTag());
        } catch (Throwable e) {
            logManager.logError(e);
        }
    }

    private <T> void handle(QueueingConsumer.Delivery delivery) throws Exception {
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("queue/" + queue);

        AMQP.BasicProperties properties = delivery.getProperties();
        String messageType = properties.getType();
        actionLog.context("messageType", messageType);

        byte[] body = delivery.getBody();
        logger.debug("body={}", LogParam.of(body));

        if (Strings.isEmpty(messageType)) throw new Error("message type must not be empty");

        actionLog.refId(properties.getCorrelationId());

        Map<String, Object> headers = properties.getHeaders();
        if (headers != null) {
            if ("true".equals(String.valueOf(headers.get(HEADER_TRACE)))) {
                actionLog.trace = true;
            }

            Object clientIP = headers.get(HEADER_CLIENT_IP);
            if (clientIP != null) {
                actionLog.context("clientIP", clientIP);
            }
        }

        String appId = properties.getAppId();
        if (appId != null) {
            actionLog.context("client", appId);
        }

        @SuppressWarnings("unchecked")
        JSONReader<T> reader = readers.get(messageType);
        if (reader == null) throw Exceptions.error("unknown message type, messageType={}", messageType);
        T message = reader.fromJSON(body);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        actionLog.context("handler", handler.getClass().getCanonicalName());

        handler.handle(message);
    }
}
