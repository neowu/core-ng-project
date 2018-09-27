package core.framework.impl.log;

import core.framework.impl.json.JSONWriter;
import core.framework.impl.kafka.ProducerMetrics;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.LogTopics;
import core.framework.impl.log.message.StatMessage;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import core.framework.util.Threads;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author neo
 */
public final class KafkaAppender implements Consumer<ActionLog> {
    public final ProducerMetrics producerMetrics = new ProducerMetrics("log-forwarder");
    private final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);
    private final BlockingQueue<ProducerRecord<byte[], byte[]>> records = new LinkedBlockingQueue<>();
    private final Producer<byte[], byte[]> producer;

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread logForwarderThread;
    private final JSONWriter<ActionLogMessage> actionLogWriter;
    private final JSONWriter<StatMessage> statWriter;
    private final Callback callback = (metadata, exception) -> {
        if (exception != null) {
            logger.warn("failed to send log message", exception);
            records.clear();
        }
    };

    public KafkaAppender(String uri) {
        var watch = new StopWatch();
        actionLogWriter = JSONWriter.of(ActionLogMessage.class);
        statWriter = JSONWriter.of(StatMessage.class);
        try {
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri,
                    ProducerConfig.ACKS_CONFIG, "0",                                    // no acknowledge to maximize performance
                    ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis(),  // metadata update timeout
                    ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy",
                    ProducerConfig.LINGER_MS_CONFIG, 50,
                    ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder");      // if not specify, kafka uses producer-${seq} name, also impact jmx naming
            var serializer = new ByteArraySerializer();
            producer = new KafkaProducer<>(config, serializer, serializer);
            producerMetrics.set(producer.metrics());
        } finally {
            logger.info("create log forwarder, uri={}, elapsed={}", uri, watch.elapsed());
        }

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started, uri={}", uri);
            while (!stop.get()) {
                try {
                    ProducerRecord<byte[], byte[]> record = records.take();
                    producer.send(record, callback);
                } catch (Throwable e) {
                    if (!stop.get()) {
                        logger.warn("failed to send log message, retry in 30 seconds", e);
                        records.clear();
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        }, "log-forwarder");
    }

    @Override
    public void accept(ActionLog log) {
        ActionLogMessage message = MessageFactory.actionLog(log);
        records.add(new ProducerRecord<>(LogTopics.TOPIC_ACTION_LOG, Strings.bytes(message.id), actionLogWriter.toJSON(message)));
    }

    public void start() {
        logForwarderThread.start();
    }

    public void stop(long timeoutInMs) {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        producer.close(timeoutInMs <= 0 ? 1000 : timeoutInMs, TimeUnit.MILLISECONDS);
    }

    public void forward(Map<String, Double> stats) {
        StatMessage message = MessageFactory.stat(stats);
        records.add(new ProducerRecord<>(LogTopics.TOPIC_STAT, Strings.bytes(message.id), statWriter.toJSON(message)));
    }
}
