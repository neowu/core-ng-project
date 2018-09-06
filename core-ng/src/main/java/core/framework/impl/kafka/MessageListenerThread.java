package core.framework.impl.kafka;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.filter.BytesParam;
import core.framework.kafka.Message;
import core.framework.log.Markers;
import core.framework.util.Maps;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final Consumer<String, byte[]> consumer;
    private final LogManager logManager;
    private final Map<String, MessageProcess<?>> processes;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final double batchLongProcessThresholdInNano;
    private final Object lock = new Object();

    MessageListenerThread(String name, Consumer<String, byte[]> consumer, MessageListener listener) {
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
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(10));
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

    private void processRecords(ConsumerRecords<String, byte[]> kafkaRecords) {
        var watch = new StopWatch();
        int count = 0;
        int size = 0;
        try {
            Map<String, List<ConsumerRecord<String, byte[]>>> messages = new LinkedHashMap<>();
            for (ConsumerRecord<String, byte[]> record : kafkaRecords) {
                messages.computeIfAbsent(record.topic(), key -> new ArrayList<>()).add(record);
                count++;
                size += record.value().length;
            }
            for (Map.Entry<String, List<ConsumerRecord<String, byte[]>>> entry : messages.entrySet()) {
                String topic = entry.getKey();
                List<ConsumerRecord<String, byte[]>> records = entry.getValue();
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

    private <T> void handle(String topic, MessageProcess<T> process, List<ConsumerRecord<String, byte[]>> records, double longProcessThresholdInNano) {
        for (ConsumerRecord<String, byte[]> record : records) {
            ActionLog actionLog = logManager.begin("=== message handling begin ===");
            try {
                actionLog.action("topic:" + topic);
                actionLog.context("topic", topic);
                actionLog.context("handler", process.handler.getClass().getCanonicalName());
                actionLog.context("key", record.key());
                logger.debug("message={}", new BytesParam(record.value()));
                actionLog.track("kafka", 0, 1, 0);

                Headers headers = record.headers();
                actionLog.refId(header(headers, MessageHeaders.HEADER_REF_ID));
                String client = header(headers, MessageHeaders.HEADER_CLIENT);
                if (client != null) actionLog.context("client", client);
                String clientIP = header(headers, MessageHeaders.HEADER_CLIENT_IP);
                if (clientIP != null) actionLog.context("clientIP", clientIP);
                if ("true".equals(header(headers, MessageHeaders.HEADER_TRACE))) {
                    actionLog.trace = true;
                }
                T message = process.reader.fromJSON(record.value());
                process.validator.validate(message);

                process.handler.handle(record.key(), message);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                long elapsed = actionLog.elapsed();
                checkSlowProcess(elapsed, longProcessThresholdInNano);
                logManager.end("=== message handling end ===");
            }
        }
    }

    private <T> void handleBulk(String topic, MessageProcess<T> process, List<ConsumerRecord<String, byte[]>> records, double longProcessThresholdInNano) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===");
        try {
            actionLog.action("topic:" + topic);
            actionLog.context("topic", topic);
            actionLog.context("handler", process.bulkHandler.getClass().getCanonicalName());
            int size = records.size();
            actionLog.track("kafka", 0, size, 0);

            Set<String> clients = new HashSet<>();
            Set<String> clientIPs = new HashSet<>();

            List<Message<T>> messages = new ArrayList<>(size);
            for (ConsumerRecord<String, byte[]> record : records) {
                Headers headers = record.headers();
                if ("true".equals(header(headers, MessageHeaders.HEADER_TRACE))) {    // trigger trace if any message is trace
                    actionLog.trace = true;
                }
                String client = header(headers, MessageHeaders.HEADER_CLIENT);
                if (client != null) {   // kafkaAppender does not send headers
                    clients.add(client);
                    clientIPs.add(header(headers, MessageHeaders.HEADER_CLIENT_IP));    // client will always send with clientIP
                }
                T message = process.reader.fromJSON(record.value());
                validate(process.validator, message, record);
                messages.add(new Message<>(record.key(), message));
            }
            if (!clients.isEmpty()) {
                logger.debug("clients={}", clients);
                logger.debug("clientIPs={}", clientIPs);
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

    private String header(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) return null;
        return new String(header.value(), UTF_8);
    }

    private <T> void validate(MessageValidator<T> validator, T value, ConsumerRecord<String, byte[]> record) {
        try {
            validator.validate(value);
        } catch (Exception e) {
            Header[] recordHeaders = record.headers().toArray();
            Map<String, String> headers = Maps.newHashMapWithExpectedSize(recordHeaders.length);
            for (Header recordHeader : recordHeaders)
                headers.put(recordHeader.key(), new String(recordHeader.value(), UTF_8));
            logger.warn("failed to validate message, key={}, headers={}, message={}", record.key(), headers, new BytesParam(record.value()), e);
            throw e;
        }
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
