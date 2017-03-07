package core.framework.impl.kafka;

import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.Message;
import core.framework.api.kafka.MessageHandler;
import core.framework.api.log.Markers;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.LogParam;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
class KafkaMessageListenerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListenerThread.class);
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Consumer<String, byte[]> consumer;
    private final Map<String, MessageHandler> handlers;
    private final Map<String, BulkMessageHandler> bulkHandlers;
    private final Map<String, JSONReader> readers;
    private final MessageValidator validator;
    private final LogManager logManager;
    private final double tooLongToProcessInNanoThreshold;

    KafkaMessageListenerThread(String name, Consumer<String, byte[]> consumer, KafkaMessageListener listener) {
        super(name);
        this.consumer = consumer;
        handlers = listener.handlers;
        bulkHandlers = listener.bulkHandlers;
        readers = listener.readers;
        validator = listener.kafka.validator;
        logManager = listener.logManager;
        tooLongToProcessInNanoThreshold = listener.kafka.maxProcessTime.toNanos() * 0.7; // 70% time to max
    }

    @Override
    public void run() {
        while (!stop.get()) {
            try {
                ConsumerRecords<String, byte[]> records = consumer.poll(Integer.MAX_VALUE);
                process(consumer, records);
            } catch (Throwable e) {
                if (!stop.get()) {
                    logger.error("failed to pull message, retry in 30 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
            }
        }
        consumer.close();
    }

    void shutdown() {
        stop.set(true);
        consumer.wakeup();
    }

    private void process(Consumer<String, byte[]> consumer, ConsumerRecords<String, byte[]> kafkaRecords) {
        StopWatch watch = new StopWatch();
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
                BulkMessageHandler<?> bulkHandler = bulkHandlers.get(topic);
                if (bulkHandler != null) {
                    handle(topic, bulkHandler, records, tooLongToProcessInNanoThreshold * records.size() / size);
                    continue;
                }
                MessageHandler<?> handler = handlers.get(topic);
                if (handler != null) {
                    handle(topic, handler, records, tooLongToProcessInNanoThreshold / size);
                }
            }
        } finally {
            consumer.commitAsync();
            logger.info("process kafka records, count={}, size={}, elapsedTime={}", count, size, watch.elapsedTime());
        }
    }

    private <T> void handle(String topic, MessageHandler<T> handler, List<ConsumerRecord<String, byte[]>> records, double tooLongToProcessInNanoThreshold) {
        @SuppressWarnings("unchecked")
        JSONReader<KafkaMessage<T>> reader = readers.get(topic);
        for (ConsumerRecord<String, byte[]> record : records) {
            logManager.begin("=== message handling begin ===");
            ActionLog actionLog = logManager.currentActionLog();
            try {
                actionLog.action("topic/" + topic);
                actionLog.context("topic", topic);
                actionLog.context("handler", handler.getClass().getCanonicalName());
                logger.debug("message={}", LogParam.of(record.value()));

                KafkaMessage<T> kafkaMessage = reader.fromJSON(record.value());

                actionLog.refId(kafkaMessage.headers.get(KafkaMessage.HEADER_REF_ID));
                String client = kafkaMessage.headers.get(KafkaMessage.HEADER_CLIENT);
                if (client != null) actionLog.context("client", client);
                String clientIP = kafkaMessage.headers.get(KafkaMessage.HEADER_CLIENT_IP);
                if (clientIP != null) actionLog.context("clientIP", clientIP);
                if ("true".equals(kafkaMessage.headers.get(KafkaMessage.HEADER_TRACE))) actionLog.trace = true;

                validator.validate(kafkaMessage.value);

                handler.handle(record.key(), kafkaMessage.value);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                long elapsedTime = actionLog.elapsedTime();
                if (elapsedTime > tooLongToProcessInNanoThreshold) {
                    logger.warn(Markers.errorCode("TOO_LONG_TO_PROCESS"), "took too long to consume message, elapsedTime={}", elapsedTime);
                }
                logManager.end("=== message handling end ===");
            }
        }
    }

    private <T> void handle(String topic, BulkMessageHandler<T> bulkHandler, List<ConsumerRecord<String, byte[]>> records, double tooLongToProcessInNanoThreshold) {
        logManager.begin("=== message handling begin ===");
        ActionLog actionLog = logManager.currentActionLog();
        try {
            @SuppressWarnings("unchecked")
            JSONReader<KafkaMessage<T>> reader = readers.get(topic);
            actionLog.action("topic/" + topic);
            actionLog.context("topic", topic);
            actionLog.context("handler", bulkHandler.getClass().getCanonicalName());
            actionLog.context("messageCount", records.size());

            List<Message<T>> messages = new ArrayList<>(records.size());
            for (ConsumerRecord<String, byte[]> record : records) {
                KafkaMessage<T> message = reader.fromJSON(record.value());
                validate(message.value, record.value());
                messages.add(new Message<>(record.key(), message.value));
                if ("true".equals(message.headers.get(KafkaMessage.HEADER_TRACE))) { // trigger trace if any message is trace
                    actionLog.trace = true;
                }
            }
            bulkHandler.handle(messages);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            long elapsedTime = actionLog.elapsedTime();
            if (elapsedTime > tooLongToProcessInNanoThreshold) {
                logger.warn(Markers.errorCode("TOO_LONG_TO_PROCESS"), "took too long to consume message, elapsedTime={}", elapsedTime);
            }
            logManager.end("=== message handling end ===");
        }
    }

    private <T> void validate(T value, byte[] recordBytes) {
        try {
            validator.validate(value);
        } catch (Exception e) {
            logger.warn("failed to validate message, message={}", LogParam.of(recordBytes), e);
            throw e;
        }
    }
}
