package core.log.service;

import core.framework.impl.json.JSONReader;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.StatMessage;
import core.framework.inject.Inject;
import core.framework.util.Lists;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static core.framework.impl.log.message.LogTopics.TOPIC_ACTION_LOG;
import static core.framework.impl.log.message.LogTopics.TOPIC_STAT;

/**
 * @author neo
 */
public class MessageProcessor {
    public final ConsumerMetrics metrics = new ConsumerMetrics();
    private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final JSONReader<ActionLogMessage> actionLogReader = JSONReader.of(ActionLogMessage.class);
    private final JSONReader<StatMessage> statReader = JSONReader.of(StatMessage.class);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final Object lock = new Object();
    @Inject
    KafkaConsumerFactory consumerFactory;
    @Inject
    ActionService actionService;
    @Inject
    StatService statService;
    @Inject
    IndexService indexService;

    private Thread processorThread;
    private Consumer<String, byte[]> consumer;

    public void start() {
        createIndexTemplates();
        processorThread.start();
    }

    public void shutdown() throws InterruptedException {
        logger.info("shutting down message processor");
        shutdown.set(true);
        consumer.wakeup();
        awaitTermination();
    }

    private void awaitTermination() throws InterruptedException {
        long end = System.currentTimeMillis() + 10000;  // timeout in 10s
        synchronized (lock) {
            while (processing.get()) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    logger.warn("failed to terminate message processor");
                    return;
                }
                lock.wait(left);
            }
        }
        logger.info("message processor stopped");
    }

    public void initialize() {
        consumer = consumerFactory.create();
        consumer.subscribe(Lists.newArrayList(TOPIC_ACTION_LOG, TOPIC_STAT));
        metrics.set(consumer.metrics());

        processorThread = new Thread(() -> {
            logger.info("message processor started, kafkaURI={}", consumerFactory.uri);
            processing.set(true);
            while (!shutdown.get()) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(10000);
                    if (records.isEmpty()) continue;
                    consume(TOPIC_ACTION_LOG, records, actionLogReader, actionService::index);
                    consume(TOPIC_STAT, records, statReader, statService::index);
                    consumer.commitAsync();
                } catch (Throwable e) {
                    if (shutdown.get()) break;
                    logger.error("failed to process message, retry in 10 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(10));
                }
            }
            consumer.close();
            processing.set(false);
            synchronized (lock) {
                lock.notifyAll();
            }
        }, "message-processor");
    }

    private void createIndexTemplates() {
        while (!shutdown.get()) {
            try {
                indexService.createIndexTemplates();
                return;
            } catch (Throwable e) {
                if (shutdown.get()) return;
                logger.error("failed to create index templates, retry in 10 seconds", e);
                Threads.sleepRoughly(Duration.ofSeconds(10));
            }
        }
    }

    private <T> void consume(String topic, ConsumerRecords<String, byte[]> records, JSONReader<T> reader, java.util.function.Consumer<List<T>> consumer) {
        int messageSize = 0;
        List<T> messages = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> record : records.records(topic)) {
            byte[] body = record.value();
            messages.add(reader.fromJSON(body));
            messageSize += body.length;
        }
        if (messages.isEmpty()) return;

        var watch = new StopWatch();
        try {
            consumer.accept(messages);
        } finally {
            long elapsedTime = watch.elapsedTime();
            logger.info("consume messages, topic={}, count={}, size={}, elapsedTime={}", topic, messages.size(), messageSize, elapsedTime);
        }
    }
}
