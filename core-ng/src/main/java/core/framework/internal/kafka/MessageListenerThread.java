package core.framework.internal.kafka;

import core.framework.internal.json.JSONReader;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.Trace;
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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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

    private final Object lock = new Object();
    private final Consumer<byte[], byte[]> consumer;

    private volatile boolean processing;

    MessageListenerThread(String name, Consumer<byte[], byte[]> consumer, MessageListener listener) {
        super(name);
        this.consumer = consumer;
        this.listener = listener;
        logManager = listener.logManager;
    }

    @Override
    public void run() {
        try {
            processing = true;
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

        logger.info("close kafka consumer, name={}", getName());
        consumer.close();
    }

    void shutdown() {
        // interrupt() will interrupt consumer coordinator,
        // the only downside of not calling interrupt() is if thread is at process->exception->sleepRoughly, shutdown will have to wait until sleep ends
        consumer.wakeup();
    }

    void awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        synchronized (lock) {
            while (processing) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    logger.warn(errorCode("FAILED_TO_STOP"), "failed to terminate kafka message listener thread, name={}", getName());
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
                    handleBulk(topic, process, records);
                } else {
                    handle(topic, process, records);
                }
            }
        } finally {
            consumer.commitAsync();
            logger.info("process kafka records, count={}, size={}, elapsed={}", count, size, watch.elapsed());
        }
    }

    <T> void handle(String topic, MessageProcess<T> process, List<ConsumerRecord<byte[], byte[]>> records) {
        for (ConsumerRecord<byte[], byte[]> record : records) {
            ActionLog actionLog = logManager.begin("=== message handling begin ===", null);
            try {
                initAction(actionLog, topic, process.handler.getClass().getCanonicalName(), process.warnings);

                actionLog.track("kafka", 0, 1, 0);

                Headers headers = record.headers();
                String trace = header(headers, MessageHeaders.HEADER_TRACE);
                if (trace != null) actionLog.trace = Trace.parse(trace);
                String correlationId = header(headers, MessageHeaders.HEADER_CORRELATION_ID);
                if (correlationId != null) actionLog.correlationIds = List.of(correlationId);
                String client = header(headers, MessageHeaders.HEADER_CLIENT);
                if (client != null) actionLog.clients = List.of(client);
                String refId = header(headers, MessageHeaders.HEADER_REF_ID);
                if (refId != null) actionLog.refIds = List.of(refId);
                logger.debug("[header] refId={}, client={}, correlationId={}, trace={}", refId, client, correlationId, trace);

                String key = key(record);
                actionLog.context.put("key", Collections.singletonList(key)); // key can be null

                long timestamp = record.timestamp();
                checkConsumerDelay(actionLog, timestamp, listener.longConsumerDelayThresholdInNano);

                byte[] value = record.value();
                logger.debug("[message] key={}, value={}, timestamp={}", key, new BytesLogParam(value), timestamp);
                T message = process.reader.fromJSON(value);
                process.validator.validate(message, false);
                process.handler.handle(key, message);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                logManager.end("=== message handling end ===");
            }
        }
    }

    <T> void handleBulk(String topic, MessageProcess<T> process, List<ConsumerRecord<byte[], byte[]>> records) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===", null);
        try {
            initAction(actionLog, topic, process.bulkHandler.getClass().getCanonicalName(), process.warnings);

            List<Message<T>> messages = messages(records, actionLog, process.reader);
            for (Message<T> message : messages) {   // validate after fromJSON, so it can track refId/correlationId
                process.validator.validate(message.value, false);
            }

            process.bulkHandler.handle(messages);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== message handling end ===");
        }
    }

    private void initAction(ActionLog actionLog, String topic, String handler, PerformanceWarning[] warnings) {
        actionLog.action("topic:" + topic);
        actionLog.warningContext.maxProcessTimeInNano(listener.maxProcessTimeInNano);
        actionLog.context.put("topic", List.of(topic));
        actionLog.context.put("handler", List.of(handler));
        logger.debug("topic={}, handler={}", topic, handler);
        if (warnings != null) actionLog.initializeWarnings(warnings);
    }

    <T> List<Message<T>> messages(List<ConsumerRecord<byte[], byte[]>> records, ActionLog actionLog, JSONReader<T> reader) throws IOException {
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
            String trace = header(headers, MessageHeaders.HEADER_TRACE);
            if (trace != null) actionLog.trace = Trace.parse(trace);   // trigger trace if any message is trace
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
            logger.debug("[message] key={}, value={}, timestamp={}, refId={}, client={}, correlationId={}, trace={}",
                key, new BytesLogParam(value), timestamp, refId, client, correlationId, trace);

            if (minTimestamp > timestamp) minTimestamp = timestamp;

            T message = reader.fromJSON(value);
            messages.add(new Message<>(key, message));
        }
        actionLog.context.put("key", new ArrayList<>(keys));    // keys could contain null

        if (!correlationIds.isEmpty()) actionLog.correlationIds = List.copyOf(correlationIds);  // action log kafka appender doesn't send headers
        if (!clients.isEmpty()) actionLog.clients = List.copyOf(clients);
        if (!refIds.isEmpty()) actionLog.refIds = List.copyOf(refIds);
        checkConsumerDelay(actionLog, minTimestamp, listener.longConsumerDelayThresholdInNano);
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

    void checkConsumerDelay(ActionLog actionLog, long timestamp, long longConsumerDelayThresholdInNano) {
        long delay = (actionLog.date.toEpochMilli() - timestamp) * 1_000_000;     // convert to nanoseconds
        logger.debug("consumerDelay={}", Duration.ofNanos(delay));
        actionLog.stats.put("consumer_delay", (double) delay);
        // refer to core.framework.internal.kafka.MessageListener.createConsumer, MAX_POLL_INTERVAL_MS_CONFIG = 30 mins
        // log as error if delay > 15 mins
        if (delay > 900_000_000_000L) {
            logger.error(errorCode("LONG_CONSUMER_DELAY"), "consumer delay is too long, delay={}", Duration.ofNanos(delay));
        } else if (delay > longConsumerDelayThresholdInNano) {
            logger.warn(errorCode("LONG_CONSUMER_DELAY"), "consumer delay is too long, delay={}", Duration.ofNanos(delay));
        }
    }
}
