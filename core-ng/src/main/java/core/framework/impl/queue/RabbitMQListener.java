package core.framework.impl.queue;

import com.rabbitmq.client.QueueingConsumer;
import core.framework.api.async.Executor;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.LogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class RabbitMQListener implements MessageHandlerConfig {
    static final String HEADER_TRACE = "trace";
    static final String HEADER_CLIENT_IP = "clientIP";

    private final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread listenerThread;
    private final String queue;
    private final Executor executor;
    private final LogManager logManager;
    private final MessageValidator validator;
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, JSONReader> readers = Maps.newHashMap();
    private int maxConcurrentHandlers = 10;
    private Semaphore semaphore;

    public RabbitMQListener(RabbitMQ rabbitMQ, String queue, Executor executor, MessageValidator validator, LogManager logManager) {
        this.executor = executor;
        this.queue = queue;
        this.validator = validator;
        this.logManager = logManager;

        listenerThread = new Thread(() -> {
            logger.info("rabbitMQ listener started, queue={}", queue);
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = rabbitMQ.consumer(queue, maxConcurrentHandlers * 2)) { // prefetch one more for each handler to improve throughput
                    pullMessages(consumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to pull message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        });
        listenerThread.setName("rabbitMQ-listener-" + queue);
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
    public MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers) {
        this.maxConcurrentHandlers = maxConcurrentHandlers;
        return this;
    }

    private void pullMessages(RabbitMQConsumer consumer) throws InterruptedException {
        String action = "queue/" + queue;
        while (!stop.get()) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            semaphore.acquire(); // acquire permit right before submit, to avoid permit failing to release back due to exception in between
            executor.submit(action, () -> {
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
        semaphore = new Semaphore(maxConcurrentHandlers, false);
        listenerThread.start();
    }

    public void stop() {
        logger.info("stop rabbitMQ listener, queue={}", queue);
        stop.set(true);
        listenerThread.interrupt();
    }

    private <T> void process(QueueingConsumer.Delivery delivery) throws Exception {
        ActionLog actionLog = logManager.currentActionLog();

        byte[] body = delivery.getBody();
        String messageType = delivery.getProperties().getType();
        actionLog.context("messageType", messageType);

        logger.debug("body={}", LogParam.of(body));

        if (Strings.isEmpty(messageType)) throw new Error("messageType must not be empty");

        actionLog.refId(delivery.getProperties().getCorrelationId());

        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers != null) {
            if ("true".equals(String.valueOf(headers.get(HEADER_TRACE)))) {
                logManager.triggerTraceLog();
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
        JSONReader<T> reader = readers.get(messageType);
        if (reader == null) {
            throw Exceptions.error("unknown message type, messageType={}", messageType);
        }
        T message = reader.fromJSON(body);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        actionLog.context("handler", handler.getClass().getCanonicalName());
        handler.handle(message);
    }
}
