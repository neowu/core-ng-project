package core.framework.impl.kafka;

import core.framework.util.StopWatch;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class MessageProducerImpl implements MessageProducer {
    public final ProducerMetrics producerMetrics;
    private final Logger logger = LoggerFactory.getLogger(MessageProducerImpl.class);
    private final String uri;
    private final String name;
    private final Producer<String, byte[]> producer;

    public MessageProducerImpl(String uri, String name) {
        if (uri == null) throw new Error("uri must not be null");
        var watch = new StopWatch();
        try {
            this.uri = uri;
            this.name = name;
            this.producerMetrics = new ProducerMetrics(name);
            Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri,
                    ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy",
                    ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
            this.producer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());
            producerMetrics.set(producer.metrics());
        } finally {
            logger.info("create kafka producer, uri={}, name={}, elapsed={}", uri, name, watch.elapsed());
        }
    }

    @Override
    public void send(ProducerRecord<String, byte[]> record) {
        producer.send(record);
    }

    public void close(long timeoutInMs) {
        if (producer != null) {
            logger.info("close kafka producer, uri={}, name={}", uri, name);
            producer.flush();
            producer.close(timeoutInMs <= 0 ? 1000 : timeoutInMs, TimeUnit.MILLISECONDS);    // close timeout must greater than 0, here use 1s to try best if no time left
        }
    }
}
