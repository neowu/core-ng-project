package core.framework.impl.kafka;

import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.Message;
import core.framework.api.kafka.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.api.util.Threads;
import core.framework.api.util.Types;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class KafkaMessageListener {
    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final Set<String> topics = Sets.newHashSet();
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, BulkMessageHandler> bulkHandlers = Maps.newHashMap();
    private final Map<String, JSONReader> readers = Maps.newHashMap();
    private final Kafka kafka;
    private final String name;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final LogManager logManager;
    public int poolSize = Runtime.getRuntime().availableProcessors() * 2;
    private Thread[] listenerThreads;

    KafkaMessageListener(Kafka kafka, String name, LogManager logManager) {
        this.kafka = kafka;
        this.name = name;
        this.logManager = logManager;
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        if (topics.contains(topic)) throw Exceptions.error("topic is already subscribed, topic={}", topic);
        topics.add(topic);
        if (handler != null) handlers.put(topic, handler);
        if (bulkHandler != null) bulkHandlers.put(topic, bulkHandler);
        readers.put(topic, JSONReader.of(Types.generic(KafkaMessage.class, messageClass)));
    }

    public void start() {
        listenerThreads = new Thread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            listenerThreads[i] = new Thread(() -> {
                logger.info("kafka listener thread started, uri={}, topics={}", kafka.uri, topics);
                while (!stop.get()) {
                    String group = logManager.appName == null ? "local" : logManager.appName;
                    try (Consumer<String, byte[]> consumer = kafka.consumer(group, topics)) {
                        while (!stop.get()) {
                            ConsumerRecords<String, byte[]> records = consumer.poll(Integer.MAX_VALUE);
                            process(consumer, records);
                        }
                    } catch (Throwable e) {
                        if (!stop.get()) {
                            logger.error("failed to pull message, retry in 30 seconds", e);
                            Threads.sleepRoughly(Duration.ofSeconds(30));
                        }
                    }
                }
            }, "kafka-listener-" + (name == null ? "" : name + "-") + i);
        }
        for (Thread thread : listenerThreads) {
            thread.start();
        }
    }

    private void process(Consumer<String, byte[]> consumer, ConsumerRecords<String, byte[]> records) {
        try {
            Map<String, List<ConsumerRecord<String, byte[]>>> messages = Maps.newLinkedHashMap();
            for (ConsumerRecord<String, byte[]> record : records) {
                messages.computeIfAbsent(record.topic(), key -> Lists.newArrayList()).add(record);
            }
            for (Map.Entry<String, List<ConsumerRecord<String, byte[]>>> entry : messages.entrySet()) {
                String topic = entry.getKey();
                BulkMessageHandler<?> bulkHandler = bulkHandlers.get(topic);
                if (bulkHandler != null) {
                    handle(topic, bulkHandler, entry.getValue());
                    continue;
                }
                MessageHandler<?> handler = handlers.get(topic);
                if (handler != null) {
                    handle(topic, handler, entry.getValue());
                }
            }
        } finally {
            consumer.commitAsync();
        }
    }

    private <T> void handle(String topic, MessageHandler<T> handler, List<ConsumerRecord<String, byte[]>> records) {
        @SuppressWarnings("unchecked")
        JSONReader<KafkaMessage<T>> reader = readers.get(topic);

        for (ConsumerRecord<String, byte[]> record : records) {
            logManager.begin("=== message handling begin ===");
            try {
                ActionLog actionLog = logManager.currentActionLog();
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

                kafka.validator.validate(kafkaMessage.value);

                handler.handle(record.key(), kafkaMessage.value);
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                logManager.end("=== message handling end ===");
            }
        }
    }

    private <T> void handle(String topic, BulkMessageHandler<T> bulkHandler, List<ConsumerRecord<String, byte[]>> records) {
        logManager.begin("=== message handling begin ===");
        try {
            @SuppressWarnings("unchecked")
            JSONReader<KafkaMessage<T>> reader = readers.get(topic);
            ActionLog actionLog = logManager.currentActionLog();
            actionLog.action("topic/" + topic);
            actionLog.context("topic", topic);
            actionLog.context("handler", bulkHandler.getClass().getCanonicalName());

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
            logManager.end("=== message handling end ===");
        }
    }

    private <T> void validate(T value, byte[] recordBytes) {
        try {
            kafka.validator.validate(value);
        } catch (Exception e) {
            logger.warn("failed to validate message, message={}", LogParam.of(recordBytes), e);
            throw e;
        }
    }

    public void stop() {
        logger.info("stop kafka listener threads, uri={}, topics={}", kafka.uri, topics);
        stop.set(true);
        for (Thread thread : listenerThreads) {
            thread.interrupt();
        }
    }
}
