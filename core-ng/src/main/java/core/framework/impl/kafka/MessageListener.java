package core.framework.impl.kafka;

import core.framework.impl.log.LogManager;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.ASCII;
import core.framework.util.StopWatch;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class MessageListener {
    public final ConsumerMetrics consumerMetrics;
    final Map<String, MessageProcess<?>> processes = new HashMap<>();
    final LogManager logManager;

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final String uri;
    private final String name;
    private final Set<String> topics = new HashSet<>();

    public int poolSize = Runtime.getRuntime().availableProcessors() * 4;
    public Duration maxProcessTime = Duration.ofMinutes(30);
    public int maxPollRecords = 500;            // default kafka setting, refer to org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
    public int maxPollBytes = 3 * 1024 * 1024;  // get 3M bytes message at max
    public int minPollBytes = 1;                // default kafka setting
    public Duration minPollMaxWaitTime = Duration.ofMillis(500);

    private MessageListenerThread[] threads;

    public MessageListener(String uri, String name, LogManager logManager) {
        this.uri = uri;
        this.name = name;
        this.logManager = logManager;
        this.consumerMetrics = new ConsumerMetrics(name);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        boolean added = topics.add(topic);
        if (!added) throw new Error(format("topic is already subscribed, topic={}", topic));
        processes.put(topic, new MessageProcess<>(handler, bulkHandler, messageClass));
    }

    public void start() {
        this.threads = createListenerThreads(); // if it fails to create thread (such kafka host is invalid, failed to create consumer), this.threads will be null to skip shutdown/awaitTermination
        for (var thread : threads) {
            thread.start();
        }
        logger.info("kafka listener started, uri={}, topics={}, name={}", uri, topics, name);
    }

    private MessageListenerThread[] createListenerThreads() {
        var threads = new MessageListenerThread[poolSize];
        var watch = new StopWatch();
        for (int i = 0; i < poolSize; i++) {
            watch.reset();
            String name = "kafka-listener-" + (this.name == null ? "" : this.name + "-") + i;
            Consumer<byte[], byte[]> consumer = consumer(topics);
            var thread = new MessageListenerThread(name, consumer, this);
            threads[i] = thread;
            logger.info("create kafka listener thread, topics={}, name={}, elapsed={}", topics, name, watch.elapsed());
        }
        return threads;
    }

    public void shutdown() {
        if (threads != null) {
            logger.info("shutting down kafka listener, uri={}, name={}", uri, name);
            for (MessageListenerThread thread : threads) {
                thread.shutdown();
            }
        }
    }

    public void awaitTermination(long timeoutInMs) {
        if (threads != null) {
            long end = System.currentTimeMillis() + timeoutInMs;
            for (MessageListenerThread thread : threads) {
                try {
                    thread.awaitTermination(end - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            logger.info("kafka listener stopped, uri={}, topics={}, name={}", uri, topics, name);
        }
    }

    private Consumer<byte[], byte[]> consumer(Set<String> topics) {
        Map<String, Object> config = Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri,   // immutable map requires value must not be null
                ConsumerConfig.GROUP_ID_CONFIG, LogManager.APP_NAME,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ASCII.toLowerCase(OffsetResetStrategy.LATEST.name()),      // refer to org.apache.kafka.clients.consumer.ConsumerConfig, must be in("latest", "earliest", "none")
                ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, (int) maxProcessTime.toMillis(),
                ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) maxProcessTime.plusSeconds(5).toMillis(),
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords,
                ConsumerConfig.FETCH_MAX_BYTES_CONFIG, maxPollBytes,
                ConsumerConfig.FETCH_MIN_BYTES_CONFIG, minPollBytes,
                ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, (int) minPollMaxWaitTime.toMillis());
        var deserializer = new ByteArrayDeserializer();
        Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(config, deserializer, deserializer);
        consumer.subscribe(topics);
        consumerMetrics.add(consumer.metrics());
        return consumer;
    }

}
