package core.framework.internal.kafka;

import core.framework.util.StopWatch;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class MessageProducer {
    public final ProducerMetrics producerMetrics;
    private final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    private final String uri;
    private final String name;
    private final Producer<byte[], byte[]> producer;

    public MessageProducer(String uri, String name) {
        var watch = new StopWatch();
        try {
            this.uri = uri;
            this.name = name;
            this.producerMetrics = new ProducerMetrics(name);
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaURI.parse(uri),
                    ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name,
                    ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60 * 1000,                        // 60s, DELIVERY_TIMEOUT_MS_CONFIG is INT type
                    ProducerConfig.LINGER_MS_CONFIG, 5L,                                         // use small linger time within acceptable range to improve batching
                    ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L,                            // longer backoff to reduce cpu usage when kafka is not available
                    ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5L * 1000,                   // 5s
                    ProducerConfig.MAX_BLOCK_MS_CONFIG, 30L * 1000);                             // 30s, metadata update timeout, shorter than default, to get exception sooner if kafka is not available

            var serializer = new ByteArraySerializer();
            this.producer = new KafkaProducer<>(config, serializer, serializer);
            producerMetrics.set(producer.metrics());
        } finally {
            logger.info("create kafka producer, uri={}, name={}, elapsed={}", uri, name, watch.elapsed());
        }
    }

    public void send(ProducerRecord<byte[], byte[]> record) {
        producer.send(record);
    }

    public void close(long timeoutInMs) {
        if (producer != null) {
            logger.info("close kafka producer, uri={}, name={}", uri, name);
            producer.flush();
            producer.close(Duration.ofMillis(timeoutInMs <= 0 ? 1000 : timeoutInMs));    // close timeout must greater than 0, here use 1s to try best if no time left
        }
    }
}
