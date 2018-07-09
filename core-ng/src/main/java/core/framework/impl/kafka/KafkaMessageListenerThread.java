package core.framework.impl.kafka;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.filter.BytesParam;
import core.framework.kafka.Message;
import core.framework.log.Markers;
import core.framework.util.Charsets;
import core.framework.util.Lists;
import core.framework.util.Maps;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
class KafkaMessageListenerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListenerThread.class);
    private final Consumer<String, byte[]> consumer;
    private final LogManager logManager;
    private final Map<String, KafkaMessageListener.MessageHandlerHolder<?>> handlerHolders;
    private final Map<String, KafkaMessageListener.BulkMessageHandlerHolder<?>> bulkHandlerHolders;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final double batchLongProcessThresholdInNano;
    private final Object lock = new Object();

    KafkaMessageListenerThread(String name, Consumer<String, byte[]> consumer, KafkaMessageListener listener) {
        super(name);
        this.consumer = consumer;
        handlerHolders = listener.handlerHolders;
        bulkHandlerHolders = listener.bulkHandlerHolders;
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
                ConsumerRecords<String, byte[]> records = consumer.poll(10000);
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
            Map<String, List<ConsumerRecord<String, byte[]>>> messages = Maps.newLinkedHashMap();
            for (ConsumerRecord<String, byte[]> record : kafkaRecords) {
                messages.computeIfAbsent(record.topic(), key -> Lists.newArrayList()).add(record);
                count++;
                size += record.value().length;
            }
            for (Map.Entry<String, List<ConsumerRecord<String, byte[]>>> entry : messages.entrySet()) {
                String topic = entry.getKey();
                List<ConsumerRecord<String, byte[]>> records = entry.getValue();
                KafkaMessageListener.BulkMessageHandlerHolder<?> bulkHandler = bulkHandlerHolders.get(topic);
                if (bulkHandler != null) {
                    handle(topic, bulkHandler, records, longProcessThreshold(batchLongProcessThresholdInNano, records.size(), count));
                } else {
                    KafkaMessageListener.MessageHandlerHolder<?> handler = handlerHolders.get(topic);
                    if (handler != null) {
                        handle(topic, handler, records, longProcessThreshold(batchLongProcessThresholdInNano, 1, count));
                    }
                }
            }
        } finally {
            consumer.commitAsync();
            logger.info("process kafka records, count={}, size={}, elapsedTime={}", count, size, watch.elapsedTime());
        }
    }

    private <T> void handle(String topic, KafkaMessageListener.MessageHandlerHolder<T> holder, List<ConsumerRecord<String, byte[]>> records, double longProcessThresholdInNano) {
        for (ConsumerRecord<String, byte[]> record : records) {
            ActionLog actionLog = logManager.begin("=== message handling begin ===");
            try {
                actionLog.action("topic:" + topic);
                actionLog.context("topic", topic);
                actionLog.context("handler", holder.handler.getClass().getCanonicalName());
                actionLog.context("key", record.key());
                logger.debug("message={}", new BytesParam(record.value()));

                T message = holder.reader.fromJSON(record.value());

                Headers headers = record.headers();
                actionLog.refId(header(headers, KafkaHeaders.HEADER_REF_ID));
                String client = header(headers, KafkaHeaders.HEADER_CLIENT);
                if (client != null) actionLog.context("client", client);
                String clientIP = header(headers, KafkaHeaders.HEADER_CLIENT_IP);
                if (clientIP != null) actionLog.context("clientIP", clientIP);
                if ("true".equals(header(headers, KafkaHeaders.HEADER_TRACE))) {
                    actionLog.trace = true;
                }

                holder.validator.validate(message);

                holder.handler.handle(record.key(), message);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                long elapsedTime = actionLog.elapsedTime();
                if (elapsedTime > longProcessThresholdInNano) {
                    logger.warn(Markers.errorCode("LONG_PROCESS"), "took too long to process message, elapsedTime={}", elapsedTime);
                }
                logManager.end("=== message handling end ===");
            }
        }
    }

    private <T> void handle(String topic, KafkaMessageListener.BulkMessageHandlerHolder<T> holder, List<ConsumerRecord<String, byte[]>> records, double longProcessThresholdInNano) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===");
        try {
            actionLog.action("topic:" + topic);
            actionLog.context("topic", topic);
            actionLog.context("handler", holder.handler.getClass().getCanonicalName());
            actionLog.stat("messageCount", records.size());
            Set<String> clients = Sets.newHashSet();
            Set<String> clientIPs = Sets.newHashSet();

            List<Message<T>> messages = new ArrayList<>(records.size());
            for (ConsumerRecord<String, byte[]> record : records) {
                T message = holder.reader.fromJSON(record.value());
                validate(holder.validator, message, record);
                messages.add(new Message<>(record.key(), message));
                Headers headers = record.headers();
                if ("true".equals(header(headers, KafkaHeaders.HEADER_TRACE))) {    // trigger trace if any message is trace
                    actionLog.trace = true;
                }
                clients.add(header(headers, KafkaHeaders.HEADER_CLIENT));
                clientIPs.add(header(headers, KafkaHeaders.HEADER_CLIENT_IP));
            }
            logger.debug("clients={}", clients);
            logger.debug("clientIPs={}", clientIPs);

            holder.handler.handle(messages);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            long elapsedTime = actionLog.elapsedTime();
            if (elapsedTime > longProcessThresholdInNano) {
                logger.warn(Markers.errorCode("LONG_PROCESS"), "took too long to process message, elapsedTime={}", elapsedTime);
            }
            logManager.end("=== message handling end ===");
        }
    }

    private String header(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) return null;
        return new String(header.value(), Charsets.UTF_8);
    }

    private <T> void validate(MessageValidator<T> validator, T value, ConsumerRecord<String, byte[]> record) {
        try {
            validator.validate(value);
        } catch (Exception e) {
            Header[] recordHeaders = record.headers().toArray();
            Map<String, String> headers = Maps.newHashMapWithExpectedSize(recordHeaders.length);
            for (Header recordHeader : recordHeaders) {
                headers.put(recordHeader.key(), new String(recordHeader.value(), Charsets.UTF_8));
            }
            logger.warn("failed to validate message, key={}, headers={}, message={}", record.key(), headers, new BytesParam(record.value()), e);
            throw e;
        }
    }

    double longProcessThreshold(double batchLongProcessThreshold, int recordCount, int totalCount) {
        return batchLongProcessThreshold * recordCount / totalCount;
    }
}
