package core.framework.impl.kafka;

import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
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
public class Kafka {
    public final ProducerMetrics producerMetrics;
    private final Logger logger = LoggerFactory.getLogger(Kafka.class);
    private final String name;
    public String uri;
    private Producer<String, byte[]> producer;
    private AdminClient admin;

    public Kafka(String name) {
        this.name = name;
        this.producerMetrics = new ProducerMetrics(name);
    }

    public Producer<String, byte[]> producer() {
        if (producer == null) {
            producer = createProducer();
        }
        return producer;
    }

    protected Producer<String, byte[]> createProducer() {
        if (uri == null) throw new Error("uri must not be null");
        StopWatch watch = new StopWatch();
        try {
            Map<String, Object> config = Maps.newHashMap();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
            config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
            Producer<String, byte[]> producer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());
            producerMetrics.set(producer.metrics());
            return producer;
        } finally {
            logger.info("create kafka producer, uri={}, name={}, elapsedTime={}", uri, name, watch.elapsedTime());
        }
    }

    public AdminClient admin() {
        if (admin == null) {
            if (uri == null) throw new Error("uri must not be null");

            StopWatch watch = new StopWatch();
            try {
                Map<String, Object> config = Maps.newHashMap();
                config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
                admin = AdminClient.create(config);
            } finally {
                logger.info("create kafka admin, uri={}, name={}, elapsedTime={}", uri, name, watch.elapsedTime());
            }
        }
        return admin;
    }

    public void close(long timeoutInMs) {
        if (producer != null) {
            logger.info("close kafka producer, name={}, uri={}", name, uri);
            producer.flush();
            producer.close(timeoutInMs <= 0 ? 1000 : timeoutInMs, TimeUnit.MILLISECONDS);    // close timeout must greater than 0, here use 1s to try best if no time left
        }
        if (admin != null) {
            logger.info("close kafka admin, name={}, uri={}", name, uri);
            admin.close(timeoutInMs <= 0 ? 1000 : timeoutInMs, TimeUnit.MILLISECONDS);
        }
    }
}
