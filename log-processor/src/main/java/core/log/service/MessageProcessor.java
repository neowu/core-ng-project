package core.log.service;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.StatMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
    private final KafkaConsumer<String, byte[]> kafkaConsumer;
    private final ActionManager actionManager;
    private final StatManager statManager;

    public MessageProcessor(String kafkaURI, ActionManager actionManager, StatManager statManager) {
        this.actionManager = actionManager;
        this.statManager = statManager;
        Map<String, Object> config = Maps.newHashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURI);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "log-processor");
        config.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3 * 1024 * 1024); // get 3M message at max
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        kafkaConsumer = new KafkaConsumer<>(config, new StringDeserializer(), new ByteArrayDeserializer());

        processorThread = new Thread(() -> {
            logger.info("message processor thread started, kafkaURI={}", kafkaURI);
            while (!stop.get()) {
                try {
                    kafkaConsumer.subscribe(Lists.newArrayList(TOPIC_ACTION_LOG, TOPIC_STAT));
                    process(kafkaConsumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
            kafkaConsumer.close();
            logger.info("message processor thread stopped");
        }, "message-processor");
    }

    public void start() {
        processorThread.start();
    }

    public void stop() {
        stop.set(true);
        kafkaConsumer.wakeup();
    }

    private void process(KafkaConsumer<String, byte[]> kafkaConsumer) {
        while (!stop.get()) {
            ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Long.MAX_VALUE);
            consume(TOPIC_ACTION_LOG, records, actionLogMessageReader, actionManager::index);
            consume(TOPIC_STAT, records, statMessageReader, statManager::index);
            kafkaConsumer.commitAsync();
        }
    }

    private <T> void consume(String topic, ConsumerRecords<String, byte[]> records, JSONReader<T> reader, Consumer<List<T>> consumer) {
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
            logger.info("consume messages, topic={}, size={}, messageSize={}, elapsedTime={}", topic, messages.size(), messageSize, elapsedTime);
        }
    }
}
