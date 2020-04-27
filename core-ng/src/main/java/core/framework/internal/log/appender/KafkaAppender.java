package core.framework.internal.log.appender;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.kafka.ProducerMetrics;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class KafkaAppender implements LogAppender {
    public final ProducerMetrics producerMetrics = new ProducerMetrics("log-forwarder");
    private final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);
    private final BlockingQueue<ProducerRecord<byte[], byte[]>> records = new LinkedBlockingQueue<>();
    private final Producer<byte[], byte[]> producer;

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread logForwarderThread;
    private final JSONMapper<ActionLogMessage> actionLogMapper;
    private final JSONMapper<StatMessage> statMapper;
    private final Callback callback = new ProducerCallback();

    public KafkaAppender(String uri) {
        var watch = new StopWatch();
        actionLogMapper = new JSONMapper<>(ActionLogMessage.class);
        statMapper = new JSONMapper<>(StatMessage.class);
        try {
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri,
                    ProducerConfig.ACKS_CONFIG, "0",                                                        // no acknowledge to maximize performance
                    ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder",                                       // if not specify, kafka uses producer-${seq} name, also impact jmx naming
                    ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name,
                    ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int) Duration.ofSeconds(60).toMillis(),     // DELIVERY_TIMEOUT_MS_CONFIG is INT type
                    ProducerConfig.LINGER_MS_CONFIG, 50,
                    ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());                 // metadata update timeout, shorter than default, to get exception sooner if kafka is not available
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
    public void append(ActionLogMessage message) {
        records.add(new ProducerRecord<>(LogTopics.TOPIC_ACTION_LOG, actionLogMapper.toJSON(message)));     // not specify message key for sticky partition
    }

    @Override
    public void append(StatMessage message) {
        records.add(new ProducerRecord<>(LogTopics.TOPIC_STAT, statMapper.toJSON(message)));    // not specify message key for sticky partition
    }

    // during startup, if it encounters configuration runtime error, logForwarderThread won't start as all startup tasks will be skipped,
    // but the failed_to_start action/trace will still be forwarded in stop() in shutdown hook
    public void start() {
        logForwarderThread.start();
    }

    public void stop(long timeoutInMs) {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        for (ProducerRecord<byte[], byte[]> record : records) {
            producer.send(record);
        }
        producer.close(Duration.ofMillis(timeoutInMs <= 0 ? 1000 : timeoutInMs));
    }

    // pmd has flaws to check slf4j log format with lambda, even with https://github.com/pmd/pmd/pull/2263, it fails to analyze logger in lambda+if condition block
    // so here use inner class as workaround
    private class ProducerCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                logger.warn("failed to send log message", exception);
                records.clear();
            }
        }
    }
}
