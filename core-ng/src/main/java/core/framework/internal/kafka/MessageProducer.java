package core.framework.internal.kafka;

import core.framework.util.StopWatch;
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

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class MessageProducer {
    public final ProducerMetrics producerMetrics;
    private final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    private final KafkaURI uri;
    private final String name;
    private final int maxRequestSize;
    private Producer<byte[], byte[]> producer;

    public MessageProducer(KafkaURI uri, String name, int maxRequestSize) {
        this.uri = uri;
        this.name = name;
        this.maxRequestSize = maxRequestSize;
        this.producerMetrics = new ProducerMetrics(name);
    }

    public void initialize() {
        producer = createProducer(uri);
    }

    public void send(ProducerRecord<byte[], byte[]> record) {
        producer.send(record, new KafkaCallback(record));
    }

    @SuppressWarnings("deprecation")
    Producer<byte[], byte[]> createProducer(KafkaURI uri) {
        var watch = new StopWatch();
        try {
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri.bootstrapURIs,
                ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name,
                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60_000,                           // 60s, DELIVERY_TIMEOUT_MS_CONFIG is INT type
                ProducerConfig.LINGER_MS_CONFIG, 5L,                                         // use small linger time within acceptable range to improve batching
                ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L,                            // longer backoff to reduce cpu usage when kafka is not available
                ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5_000L,                      // 5s
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 30_000L,                                 // 30s, metadata update timeout, shorter than default, to get exception sooner if kafka is not available
                ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize,
                ProducerConfig.AUTO_INCLUDE_JMX_REPORTER_CONFIG, Boolean.FALSE);

            var serializer = new ByteArraySerializer();
            var producer = new KafkaProducer<>(config, serializer, serializer);
            producerMetrics.set(producer.metrics());
            return producer;
        } finally {
            logger.info("create kafka producer, uri={}, name={}, elapsed={}", uri, name, watch.elapsed());
        }
    }

    public void close(long timeoutInMs) {
        if (producer != null) {
            logger.info("close kafka producer, uri={}, name={}", uri, name);
            producer.flush();
            producer.close(Duration.ofMillis(timeoutInMs));    // close timeout must greater than 0, the shutdown hook always pass in positive timeout
        }
    }

    static final class KafkaCallback implements Callback {
        private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCallback.class);
        private final ProducerRecord<byte[], byte[]> record;

        KafkaCallback(ProducerRecord<byte[], byte[]> record) {
            this.record = record;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {    // if failed to send message (kafka is down), fallback to error output
                byte[] key = record.key();
                LOGGER.error("failed to send kafka message, error={}, topic={}, key={}, value={}",
                    exception.getMessage(),
                    record.topic(),
                    key == null ? null : new String(key, UTF_8),
                    new String(record.value(), UTF_8),
                    exception);
            }
        }
    }
}
