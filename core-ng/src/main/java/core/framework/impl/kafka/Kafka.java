package core.framework.impl.kafka;

import core.framework.api.util.Maps;
import core.framework.impl.log.LogManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class Kafka {
    private final Logger logger = LoggerFactory.getLogger(Kafka.class);
    private final String name;
    private final LogManager logManager;
    public String uri;
    public MessageValidator validator = new MessageValidator();
    private KafkaMessageListener listener;
    private Producer<String, byte[]> producer;

    public Kafka(String name, LogManager logManager) {
        this.name = name;
        this.logManager = logManager;
    }

    public Producer<String, byte[]> producer() {
        if (producer == null) {
            if (uri == null) throw new Error("uri is required, please configure kafka.uri() first");
            Map<String, Object> config = Maps.newHashMap();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
            config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
            producer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());
        }
        return producer;
    }

    public Consumer<String, byte[]> consumer(String group, Set<String> topics) {
        if (uri == null) throw new Error("uri is required, please configure kafka.uri() first");
        Map<String, Object> config = Maps.newHashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3 * 1024 * 1024); // get 3M message at max
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(config, new StringDeserializer(), new ByteArrayDeserializer());
        consumer.subscribe(topics);
        return consumer;
    }

    public KafkaMessageListener listener() {
        if (listener == null) {
            listener = new KafkaMessageListener(this, name, logManager);
        }
        return listener;
    }

    public void close() {
        if (listener != null) listener.stop();
        if (producer != null) {
            logger.info("close kafka producer, uri={}", uri);
            producer.flush();
            producer.close();
        }
    }

    public void initialize() {
        if (listener != null) listener.start();
    }
}
