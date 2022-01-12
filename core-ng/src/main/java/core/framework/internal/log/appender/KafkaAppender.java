package core.framework.internal.log.appender;

import core.framework.internal.json.JSONWriter;
import core.framework.internal.kafka.KafkaURI;
import core.framework.internal.kafka.ProducerMetrics;
import core.framework.log.LogAppender;
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

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author neo
 */
public final class KafkaAppender implements LogAppender {
    public final ProducerMetrics producerMetrics = new ProducerMetrics("log-forwarder");

    final BlockingQueue<ProducerRecord<byte[], byte[]>> records = new LinkedBlockingQueue<>();
    private final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);
    private final Thread logForwarderThread;
    private final JSONWriter<ActionLogMessage> actionLogWriter = new JSONWriter<>(ActionLogMessage.class);
    private final JSONWriter<StatMessage> statWriter = new JSONWriter<>(StatMessage.class);
    private final Callback callback = new KafkaCallback();
    private final KafkaURI uri;

    private Producer<byte[], byte[]> producer;
    private volatile boolean stop;

    public KafkaAppender(KafkaURI uri) {
        this.uri = uri;
        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started, uri={}", this.uri);
            initialize();
            process();
        }, "log-forwarder");
    }

    void initialize() {
        while (!stop) {
            if (resolveURI(uri)) {
                producer = createProducer(uri);
                break;
            }
            logger.warn("failed to resolve log kafka uri, retry in 10 seconds, uri={}", this.uri);
            records.clear();    // throw away records, to prevent from high heap usage
            Threads.sleepRoughly(Duration.ofSeconds(10));
        }
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

    KafkaProducer<byte[], byte[]> createProducer(KafkaURI uri) {
        var watch = new StopWatch();
        try {
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri.bootstrapURIs,
                ProducerConfig.ACKS_CONFIG, "0",                                        // no acknowledge to maximize performance
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false,                            // since kafka 3.0.0, "enable.idempotence" is true by default, and it overrides "acks" to all
                ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder",                       // if not specify, kafka uses producer-${seq} name, also impact jmx naming
                ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name,
                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60_000,                      // 60s, type is INT
                ProducerConfig.LINGER_MS_CONFIG, 50L,
                ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L,                       // longer backoff to reduce cpu usage when kafka is not available
                ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5_000L,                 // 5s
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 30_000L);                           // 30s, metadata update timeout, shorter than default, to get exception sooner if kafka is not available
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
        // not specify message key for sticky partition, StickyPartitionCache will be used if key is null
        // refer to org.apache.kafka.clients.producer.internals.DefaultPartitioner.partition
        records.add(new ProducerRecord<>(LogTopics.TOPIC_ACTION_LOG, actionLogWriter.toJSON(message)));
    }

    @Override
    public void append(StatMessage message) {
        records.add(new ProducerRecord<>(LogTopics.TOPIC_STAT, statWriter.toJSON(message)));    // not specify message key for sticky partition
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

        if (producer == null && resolveURI(uri)) producer = createProducer(uri);           // producer can be null if app failed to start (exception thrown by configure(), startup hook will not run)
        if (producer != null) {                                         // producer can be null if uri is not resolved
            for (ProducerRecord<byte[], byte[]> record : records) {     // if log-kafka is not available, here will block MAX_BLOCK_MS, to simplify it's ok not handling timeout since kafka appender is at end of shutdown, no more critical resources left to handle
                producer.send(record);
            }
            producer.close(Duration.ofMillis(timeoutInMs));
        }
    }

    boolean resolveURI(KafkaURI kafkaURI) {
        for (String uri : kafkaURI.bootstrapURIs) {
            int index = uri.indexOf(':');
            if (index == -1) throw new Error("invalid kafka uri, uri=" + uri);
            String host = uri.substring(0, index);
            var address = new InetSocketAddress(host, 9092);
            if (!address.isUnresolved()) {
                return true;    // break if any uri is resolvable
            }
        }
        return false;
    }

    // pmd has flaws to check slf4j log format with lambda, even with https://github.com/pmd/pmd/pull/2263, it fails to analyze logger in lambda+if condition block
    // so here use inner class as workaround
    class KafkaCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                logger.warn("failed to send log message", exception);
                records.clear();
            }
        }
    }
}
