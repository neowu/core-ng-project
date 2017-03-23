package core.log.service;

import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.StatMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class MessageProcessor {
    private static final String TOPIC_ACTION_LOG = "action-log";
    private static final String TOPIC_STAT = "stat";

    private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread processorThread;
    private final JSONReader<ActionLogMessage> actionLogMessageReader = JSONReader.of(ActionLogMessage.class);
    private final JSONReader<StatMessage> statMessageReader = JSONReader.of(StatMessage.class);
    private final Consumer<String, byte[]> consumer;

    @Inject
    public MessageProcessor(KafkaConsumerFactory consumerFactory, ActionService actionService, StatService statService, IndexService indexService) {
        consumer = consumerFactory.create();
        consumer.subscribe(Lists.newArrayList(TOPIC_ACTION_LOG, TOPIC_STAT));

        processorThread = new Thread(() -> {
            logger.info("message processor started, kafkaURI={}", consumerFactory.uri);
            createIndexTemplates(indexService);
            while (!stop.get()) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Long.MAX_VALUE);
                    consume(TOPIC_ACTION_LOG, records, actionLogMessageReader, actionService::index);
                    consume(TOPIC_STAT, records, statMessageReader, statService::index);
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

    private void createIndexTemplates(IndexService indexService) {
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

    public void start() {
        processorThread.start();
    }

    public void stop() {
        stop.set(true);
        consumer.wakeup();
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
