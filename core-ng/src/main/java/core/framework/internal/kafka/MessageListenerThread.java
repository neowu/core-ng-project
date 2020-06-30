package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.kafka.Message;
import core.framework.util.Sets;
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

import static core.framework.log.Markers.errorCode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
class MessageListenerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MessageListenerThread.class);
    private final MessageListener listener;
    private final LogManager logManager;
    private final double batchLongProcessThresholdInNano;
    private final long longConsumerLagThresholdInMs;

    private final Object lock = new Object();
    private Consumer<byte[], byte[]> consumer;

    private volatile boolean processing;

    MessageListenerThread(String name, MessageListener listener) {
        super(name);
        this.listener = listener;
        logManager = listener.logManager;
        batchLongProcessThresholdInNano = listener.maxProcessTime.toNanos() * 0.7; // 70% time of max
        longConsumerLagThresholdInMs = listener.longConsumerLagThreshold.toMillis();
    }

    @Override
    public void run() {
        try {
            processing = true;
            consumer = listener.createConsumer();
            process();
        } finally {
            processing = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private void process() {
        while (!listener.shutdown) {
            try {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofSeconds(30));    // consumer should call poll at least once every MAX_POLL_INTERVAL_MS
                if (records.isEmpty()) continue;
                processRecords(records);
            } catch (Throwable e) {
                if (!listener.shutdown) {
                    logger.error("failed to pull message, retry in 10 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(10));
                }
            }
        }
        if (consumer != null) { // consumer can be null if host is not resolvable
            logger.info("close kafka consumer, name={}", getName());
            consumer.close();
        }
    }

    void shutdown() {
        if (consumer == null) {
            interrupt();    // interrupt listener.createConsumer() if needed
        } else {
            // if consumer != null, interrupt() will interrupt consumer coordinator,
            // the only downside of not calling interrupt() is if thread is at process->exception->sleepRoughly, shutdown will have to wait until sleep ends
            consumer.wakeup();
        }
    }

    void awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        synchronized (lock) {
            while (processing) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    logger.warn("failed to terminate kafka message listener thread, name={}", getName());
                    break;
                }
                lock.wait(left);
            }
        }
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
                MessageProcess<?> process = listener.processes.get(topic);
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

                String key = key(record);
                actionLog.context("key", key);

                long timestamp = record.timestamp();
                logger.debug("[message] timestamp={}", timestamp);
                long lag = actionLog.date.toEpochMilli() - timestamp;
                actionLog.stat("consumer_lag_in_ms", lag);
                checkConsumerLag(lag, longConsumerLagThresholdInMs);

                byte[] value = record.value();
                logger.debug("[message] value={}", new BytesLogParam(value));
                T message = process.mapper.fromJSON(value);
                process.validator.validate(message, false);
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
            for (Message<T> message : messages) {   // validate after fromJSON, so it can track refId/correlationId
                process.validator.validate(message.value, false);
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
        Set<String> keys = Sets.newHashSetWithExpectedSize(size);
        long minTimestamp = Long.MAX_VALUE;

        for (ConsumerRecord<byte[], byte[]> record : records) {
            Headers headers = record.headers();
            if ("true".equals(header(headers, MessageHeaders.HEADER_TRACE))) actionLog.trace = true;    // trigger trace if any message is trace
            String correlationId = header(headers, MessageHeaders.HEADER_CORRELATION_ID);
            if (correlationId != null) correlationIds.add(correlationId);
            String client = header(headers, MessageHeaders.HEADER_CLIENT);
            if (client != null) clients.add(client);
            String refId = header(headers, MessageHeaders.HEADER_REF_ID);
            if (refId != null) refIds.add(refId);

            String key = key(record);
            keys.add(key);

            byte[] value = record.value();
            long timestamp = record.timestamp();
            logger.debug("[message] key={}, value={}, timestamp={}, refId={}, client={}, correlationId={}",
                    key, new BytesLogParam(value), timestamp, refId, client, correlationId);

            if (minTimestamp > timestamp) minTimestamp = timestamp;

            T message = mapper.fromJSON(value);
            messages.add(new Message<>(key, message));
        }
        actionLog.context("key", keys.toArray());

        if (!correlationIds.isEmpty()) actionLog.correlationIds = List.copyOf(correlationIds);  // action log kafka appender doesn't send headers
        if (!clients.isEmpty()) actionLog.clients = List.copyOf(clients);
        if (!refIds.isEmpty()) actionLog.refIds = List.copyOf(refIds);
        long lag = actionLog.date.toEpochMilli() - minTimestamp;
        actionLog.stat("consumer_lag_in_ms", lag);
        checkConsumerLag(lag, longConsumerLagThresholdInMs);
        return messages;
    }

    String key(ConsumerRecord<byte[], byte[]> record) {
        byte[] key = record.key();
        return key == null ? null : new String(key, UTF_8);
    }

    String header(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) return null;
        return new String(header.value(), UTF_8);
    }

    void checkSlowProcess(long elapsed, double longProcessThreshold) {
        if (elapsed > longProcessThreshold) {
            logger.warn(errorCode("LONG_PROCESS"), "took too long to process message, elapsed={}", elapsed);
        }
    }

    void checkConsumerLag(long lagInMs, long longConsumerLagThresholdInMs) {
        if (lagInMs > longConsumerLagThresholdInMs) {
            logger.warn(errorCode("LONG_CONSUMER_LAG"), "consumer delay is too long, lag={}", Duration.ofMillis(lagInMs));
        }
    }

    double longProcessThreshold(double batchLongProcessThreshold, int recordCount, int totalCount) {
        return batchLongProcessThreshold * recordCount / totalCount;
    }
}
