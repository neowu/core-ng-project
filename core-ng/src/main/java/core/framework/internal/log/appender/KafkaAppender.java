package core.framework.internal.log.appender;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.kafka.KafkaURI;
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

/**
 * @author neo
 */
public final class KafkaAppender implements LogAppender {
    public final ProducerMetrics producerMetrics = new ProducerMetrics("log-forwarder");

    private final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);
    private final BlockingQueue<ProducerRecord<byte[], byte[]>> records = new LinkedBlockingQueue<>();
    private final Thread logForwarderThread;
    private final JSONMapper<ActionLogMessage> actionLogMapper;
    private final JSONMapper<StatMessage> statMapper;
    private final Callback callback = new KafkaCallback();

    private Producer<byte[], byte[]> producer;
    private volatile boolean stop;

    public KafkaAppender(KafkaURI uri) {
        actionLogMapper = new JSONMapper<>(ActionLogMessage.class);
        statMapper = new JSONMapper<>(StatMessage.class);

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started, uri={}", uri);

            while (!stop) {
                if (uri.resolveURI()) {
                    producer = createProducer(uri);
                    break;
                }
                logger.warn("failed to resolve log kafka uri, retry in 10 seconds, uri={}", uri);
                records.clear();    // throw away records, to prevent from high heap usage
                Threads.sleepRoughly(Duration.ofSeconds(10));
            }

            process();
        }, "log-forwarder");
    }

    private void process() {
        while (!stop) {
            try {
                ProducerRecord<byte[], byte[]> record = records.take();
                producer.send(record, callback);
            } catch (Throwable e) {
                if (!stop) {    // if during stop and records.take() is interrupted, not clear records and sleep
                    logger.warn("failed to send log message, retry in 30 seconds", e);
                    records.clear();
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
            }
        }
    }

    private KafkaProducer<byte[], byte[]> createProducer(KafkaURI uri) {
        var watch = new StopWatch();
        try {
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri.bootstrapURIs,
                    ProducerConfig.ACKS_CONFIG, "0",                                        // no acknowledge to maximize performance
                    ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder",                       // if not specify, kafka uses producer-${seq} name, also impact jmx naming
                    ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name,
                    ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60 * 1000,                   // 60s, type is INT
                    ProducerConfig.LINGER_MS_CONFIG, 50L,
                    ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L,                        // longer backoff to reduce cpu usage when kafka is not available
                    ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5L * 1000,               // 5s
                    ProducerConfig.MAX_BLOCK_MS_CONFIG, 30L * 1000);                         // 30s, metadata update timeout, shorter than default, to get exception sooner if kafka is not available
            var serializer = new ByteArraySerializer();
            var producer = new KafkaProducer<>(config, serializer, serializer);
            producerMetrics.set(producer.metrics());
            return producer;
        } finally {
            logger.info("create kafka log producer, uri={}, elapsed={}", uri, watch.elapsed());
        }
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
        stop = true;
        logForwarderThread.interrupt();
        if (producer != null) {     // producer can be null if uri is not resolved
            for (ProducerRecord<byte[], byte[]> record : records) {     // if log-kafka is not available, here will block MAX_BLOCK_MS, to simplify it's ok not handling timeout since kafka appender is at end of shutdown, no more critical resources left to handle
                producer.send(record);
            }
            producer.close(Duration.ofMillis(timeoutInMs <= 0 ? 1000 : timeoutInMs));
        }
    }

    // pmd has flaws to check slf4j log format with lambda, even with https://github.com/pmd/pmd/pull/2263, it fails to analyze logger in lambda+if condition block
    // so here use inner class as workaround
    private class KafkaCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                logger.warn("failed to send log message", exception);
                records.clear();
            }
        }
    }
}
