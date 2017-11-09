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
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final JSONReader<ActionLogMessage> actionLogReader = JSONReader.of(ActionLogMessage.class);
    private final JSONReader<StatMessage> statReader = JSONReader.of(StatMessage.class);
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

    public void stop() {
        stop.set(true);
        consumer.wakeup();
    }

    public void initialize() {
        consumer = consumerFactory.create();
        consumer.subscribe(Lists.newArrayList(TOPIC_ACTION_LOG, TOPIC_STAT));
        metrics.set(consumer.metrics());

        processorThread = new Thread(() -> {
            logger.info("message processor started, kafkaURI={}", consumerFactory.uri);
            while (!stop.get()) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    consume(TOPIC_ACTION_LOG, records, actionLogReader, actionService::index);
                    consume(TOPIC_STAT, records, statReader, statService::index);
                    consumer.commitAsync();
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
            consumer.close();
            logger.info("message processor stopped");
        }, "message-processor");
    }

    private void createIndexTemplates() {
        while (!stop.get()) {
            try {
                indexService.createIndexTemplates();
                break;
            } catch (Throwable e) {
                if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                    logger.error("failed to create index templates, retry in 30 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
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

        StopWatch watch = new StopWatch();
        try {
            consumer.accept(messages);
        } finally {
            long elapsedTime = watch.elapsedTime();
            logger.info("consume messages, topic={}, count={}, size={}, elapsedTime={}", topic, messages.size(), messageSize, elapsedTime);
        }
    }
}
