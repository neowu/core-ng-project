package core.framework.internal.kafka;

import core.framework.internal.log.LogManager;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.Maps;
import core.framework.util.Network;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public class MessageListener {
    private static final AtomicInteger CONSUMER_CLIENT_ID_SEQUENCE = new AtomicInteger(1);
    public final ConsumerMetrics consumerMetrics;
    final Map<String, MessageProcess<?>> processes = new HashMap<>();
    final LogManager logManager;

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final KafkaURI uri;
    private final String name;
    private final Set<String> topics = new HashSet<>();

    public int poolSize = Runtime.getRuntime().availableProcessors() * 4;
    public Duration maxProcessTime = Duration.ofMinutes(30);
    public Duration longConsumerDelayThreshold = Duration.ofSeconds(60);
    public int maxPollRecords = 500;            // default kafka setting, refer to org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
    public int maxPollBytes = 3 * 1024 * 1024;  // get 3M bytes if possible for batching, this is not absolute limit of max bytes to poll, refer to org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_DOC
    public int minPollBytes = 1;                // default kafka setting
    public Duration maxWaitTime = Duration.ofMillis(500);
    public String groupId = LogManager.APP_NAME;

    volatile boolean shutdown;
    private MessageListenerThread[] threads;

    public MessageListener(KafkaURI uri, String name, LogManager logManager) {
        this.uri = uri;
        this.name = name;
        this.logManager = logManager;
        this.consumerMetrics = new ConsumerMetrics(name);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        boolean added = topics.add(topic);
        if (!added) throw new Error("topic is already subscribed, topic=" + topic);
        processes.put(topic, new MessageProcess<>(handler, bulkHandler, messageClass));
    }

    public void start() {
        threads = new MessageListenerThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            String name = listenerThreadName(this.name, i);
            threads[i] = new MessageListenerThread(name, this);
        }
        for (var thread : threads) {
            thread.start();
        }
        logger.info("kafka listener started, uri={}, topics={}, name={}, groupId={}", uri, topics, name, groupId);
    }

    String listenerThreadName(String name, int index) {
        return "kafka-listener-" + (name == null ? "" : name + "-") + index;
    }

    public void shutdown() {
        shutdown = true;
        if (threads != null) {  // in case of shutdown before startup finish
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

    Consumer<byte[], byte[]> createConsumer() {
        synchronized (this) {
            while (!shutdown) {
                if (uri.resolveURI()) {
                    return consumer();
                }
                logger.warn("failed to resolve kafka uri, retry in 10 seconds, uri={}", uri);
                Threads.sleepRoughly(Duration.ofSeconds(10));
            }
            return null;
        }
    }

    private Consumer<byte[], byte[]> consumer() {
        var watch = new StopWatch();
        try {
            Map<String, Object> config = Maps.newHashMapWithExpectedSize(12);
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri.bootstrapURIs);
            config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            config.put(ConsumerConfig.CLIENT_ID_CONFIG, Network.LOCAL_HOST_NAME + "-" + CONSUMER_CLIENT_ID_SEQUENCE.getAndIncrement());      // will show in monitor metrics
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");                      // refer to org.apache.kafka.clients.consumer.ConsumerConfig, must be in("latest", "earliest", "none")
            config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, (int) maxProcessTime.toMillis());
            config.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L);                       // longer backoff to reduce cpu usage when kafka is not available
            config.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5_000L);                 // 5s
            config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
            config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPollBytes);
            config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, minPollBytes);
            config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, (int) maxWaitTime.toMillis());
            var deserializer = new ByteArrayDeserializer();
            Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(config, deserializer, deserializer);
            consumerMetrics.add(consumer.metrics());

            consumer.subscribe(topics);
            return consumer;
        } finally {
            logger.info("create kafka consumer, topics={}, name={}, elapsed={}", topics, name, watch.elapsed());
        }
    }
}
