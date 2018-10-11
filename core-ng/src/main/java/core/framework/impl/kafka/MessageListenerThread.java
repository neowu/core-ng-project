package core.framework.impl.kafka;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.filter.BytesLogParam;
import core.framework.internal.json.JSONMapper;
import core.framework.kafka.Message;
import core.framework.log.Markers;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
class MessageListenerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MessageListenerThread.class);
    private final Consumer<byte[], byte[]> consumer;
    private final LogManager logManager;
    private final Map<String, MessageProcess<?>> processes;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final double batchLongProcessThresholdInNano;
    private final Object lock = new Object();

    MessageListenerThread(String name, Consumer<byte[], byte[]> consumer, MessageListener listener) {
        super(name);
        this.consumer = consumer;
        processes = listener.processes;
        logManager = listener.logManager;
        batchLongProcessThresholdInNano = listener.maxProcessTime.toNanos() * 0.7; // 70% time of max
    }

    @Override
    public void run() {
        try {
            processing.set(true);
            process();
        } finally {
            processing.set(false);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    void shutdown() {
        shutdown.set(true);
        consumer.wakeup();
    }

    void awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        synchronized (lock) {
            while (processing.get()) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    logger.warn("failed to terminate kafka message listener thread, name={}", getName());
                    break;
                }
                lock.wait(left);
            }
        }
    }

    private void process() {
        while (!shutdown.get()) {
            try {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofSeconds(10));
                if (records.isEmpty()) continue;
                processRecords(records);
            } catch (Throwable e) {
                if (shutdown.get()) break;
                logger.error("failed to pull message, retry in 10 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(10));
            }
        }
        logger.info("close kafka consumer, name={}", getName());
        consumer.close();
    }

    private void processRecords(ConsumerRecords<byte[], byte[]> kafkaRecords) {
        var watch = new StopWatch();
        int count = 0;
        int size = 0;
        try {
            Map<String, List<ConsumerRecord<byte[], byte[]>>> messages = new HashMap<>();     // record in one topic maintains order
            for (ConsumerRecord<byte[], byte[]> record : kafkaRecords) {
                messages.computeIfAbsent(record.topic(), key -> new ArrayList<>()).add(record);
                count++;
                size += record.value().length;
            }
            for (Map.Entry<String, List<ConsumerRecord<byte[], byte[]>>> entry : messages.entrySet()) {
                String topic = entry.getKey();
                List<ConsumerRecord<byte[], byte[]>> records = entry.getValue();
                MessageProcess<?> process = processes.get(topic);
                if (process.bulkHandler != null) {
                    handleBulk(topic, process, records, longProcessThreshold(batchLongProcessThresholdInNano, records.size(), count));
                } else {
                    handle(topic, process, records, longProcessThreshold(batchLongProcessThresholdInNano, 1, count));
                }
            }
        } finally {
            consumer.commitAsync();
            logger.info("process kafka records, count={}, size={}, elapsed={}", count, size, watch.elapsed());
        }
    }

    private <T> void handle(String topic, MessageProcess<T> process, List<ConsumerRecord<byte[], byte[]>> records, double longProcessThresholdInNano) {
        for (ConsumerRecord<byte[], byte[]> record : records) {
            ActionLog actionLog = logManager.begin("=== message handling begin ===");
            try {
                actionLog.action("topic:" + topic);
                actionLog.context("topic", topic);
                actionLog.context("handler", process.handler.getClass().getCanonicalName());
                actionLog.track("kafka", 0, 1, 0);

                Headers headers = record.headers();
                if ("true".equals(header(headers, MessageHeaders.HEADER_TRACE))) actionLog.trace = true;
                String correlationId = header(headers, MessageHeaders.HEADER_CORRELATION_ID);
                if (correlationId != null) actionLog.correlationIds = List.of(correlationId);
                String client = header(headers, MessageHeaders.HEADER_CLIENT);
                if (client != null) actionLog.clients = List.of(client);
                String refId = header(headers, MessageHeaders.HEADER_REF_ID);
                if (refId != null) actionLog.refIds = List.of(refId);
                logger.debug("[header] refId={}, client={}, correlationId={}", refId, client, correlationId);

                String key = new String(record.key(), UTF_8);   // key will be not null in our system
                actionLog.context("key", key);

                byte[] value = record.value();
                logger.debug("[message] value={}", new BytesLogParam(value));
                T message = process.mapper.fromJSON(value);
                process.validator.validate(message);
                process.handler.handle(key, message);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                long elapsed = actionLog.elapsed();
                checkSlowProcess(elapsed, longProcessThresholdInNano);
                logManager.end("=== message handling end ===");
            }
        }
    }

    private <T> void handleBulk(String topic, MessageProcess<T> process, List<ConsumerRecord<byte[], byte[]>> records, double longProcessThresholdInNano) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===");
        try {
            actionLog.action("topic:" + topic);
            actionLog.context("topic", topic);
            actionLog.context("handler", process.bulkHandler.getClass().getCanonicalName());

            List<Message<T>> messages = messages(records, actionLog, process.mapper);
            for (Message<T> message : messages) {
                process.validator.validate(message.value);
            }

            process.bulkHandler.handle(messages);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            long elapsed = actionLog.elapsed();
            checkSlowProcess(elapsed, longProcessThresholdInNano);
            logManager.end("=== message handling end ===");
        }
    }

    <T> List<Message<T>> messages(List<ConsumerRecord<byte[], byte[]>> records, ActionLog actionLog, JSONMapper<T> mapper) {
        int size = records.size();
        actionLog.track("kafka", 0, size, 0);
        List<Message<T>> messages = new ArrayList<>(size);
        Set<String> correlationIds = new HashSet<>();
        Set<String> clients = new HashSet<>();
        Set<String> refIds = new HashSet<>();

        for (ConsumerRecord<byte[], byte[]> record : records) {
            Headers headers = record.headers();
            if ("true".equals(header(headers, MessageHeaders.HEADER_TRACE))) actionLog.trace = true;    // trigger trace if any message is trace
            String correlationId = header(headers, MessageHeaders.HEADER_CORRELATION_ID);
            if (correlationId != null) correlationIds.add(correlationId);
            String client = header(headers, MessageHeaders.HEADER_CLIENT);
            if (client != null) clients.add(client);
            String refId = header(headers, MessageHeaders.HEADER_REF_ID);
            if (refId != null) refIds.add(refId);

            String key = new String(record.key(), UTF_8);    // key will not be null in our system
            byte[] value = record.value();
            logger.debug("[message] key={}, value={}, refId={}, client={}, correlationId={}", key, new BytesLogParam(value), refId, client, correlationId);

            T message = mapper.fromJSON(value);
            messages.add(new Message<>(key, message));
        }

        if (!correlationIds.isEmpty()) actionLog.correlationIds = List.copyOf(correlationIds);  // action log kafka appender doesn't send headers
        if (!clients.isEmpty()) actionLog.clients = List.copyOf(clients);
        if (!refIds.isEmpty()) actionLog.refIds = List.copyOf(refIds);
        return messages;
    }

    String header(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) return null;
        return new String(header.value(), UTF_8);
    }

    private void checkSlowProcess(long elapsed, double longProcessThreshold) {
        if (elapsed > longProcessThreshold) {
            logger.warn(Markers.errorCode("LONG_PROCESS"), "took too long to process message, elapsed={}", elapsed);
        }
    }

    double longProcessThreshold(double batchLongProcessThreshold, int recordCount, int totalCount) {
        return batchLongProcessThreshold * recordCount / totalCount;
    }
}
