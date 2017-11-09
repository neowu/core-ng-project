package core.log.service;

import core.framework.util.Maps;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Map;

/**
 * @author neo
 */
public class KafkaConsumerFactory {
    final String uri;

    public KafkaConsumerFactory(String uri) {
        this.uri = uri;
    }

    public Consumer<String, byte[]> create() {
        Map<String, Object> config = Maps.newHashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "log-processor");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);
        config.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3 * 1024 * 1024); // get 3M message at max
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024 * 1024);     // try to get at least 1M message
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);           // pause 500ms if not enough data to process

        return new KafkaConsumer<>(config, new StringDeserializer(), new ByteArrayDeserializer());
    }
}
