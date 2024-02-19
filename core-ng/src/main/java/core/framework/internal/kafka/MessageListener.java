package core.framework.internal.kafka;

import core.framework.internal.log.LogManager;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.Maps;
import core.framework.util.Network;
import core.framework.util.StopWatch;
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

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public class MessageListener {
    public final ConsumerMetrics consumerMetrics;
    public final Set<String> topics = new HashSet<>();
    public final Map<String, MessageProcess<?>> processes = new HashMap<>();
    public final Map<String, MessageProcess<?>> bulkProcesses = new HashMap<>();
    final LogManager logManager;

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final KafkaURI uri;
    private final String name;

    public int concurrency = Runtime.getRuntime().availableProcessors() * 16;
    public long longConsumerDelayThresholdInNano = Duration.ofSeconds(30).toNanos();
    public int maxPollRecords = 500;            // default kafka setting, refer to org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
    public int maxPollBytes = 3 * 1024 * 1024;  // get 3M bytes if possible for batching, this is not absolute limit of max bytes to poll, refer to org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_DOC
    public int minPollBytes = 1;                // default kafka setting
    public Duration maxWaitTime = Duration.ofMillis(500);
    public String groupId = LogManager.APP_NAME;

    long maxProcessTimeInNano;
    private MessageListenerThread thread;

    public MessageListener(KafkaURI uri, String name, LogManager logManager, long maxProcessTimeInNano) {
        this.uri = uri;
        this.name = name;
        this.logManager = logManager;
        this.maxProcessTimeInNano = maxProcessTimeInNano;
        this.consumerMetrics = new ConsumerMetrics(name);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        boolean added = topics.add(topic);
        if (!added) throw new Error("topic is already subscribed, topic=" + topic);
        if (handler != null) {
            processes.put(topic, new MessageProcess<>(handler, messageClass));
        } else {
            bulkProcesses.put(topic, new MessageProcess<>(bulkHandler, messageClass));
        }
    }

    public void start() {
        Consumer<String, byte[]> consumer = createConsumer();
        thread = new MessageListenerThread(threadName(name), consumer, this);
        thread.start();
        logger.info("kafka listener started, uri={}, topics={}, name={}, groupId={}", uri, topics, name, groupId);
    }

    public void shutdown() {
        if (thread != null) {  // in case of shutdown in middle of start
            logger.info("shutting down kafka listener, uri={}, name={}", uri, name);
            thread.shutdown();
        }
    }

    public void awaitTermination(long timeoutInMs) {
        if (thread != null) {
            try {
                boolean success = thread.awaitTermination(timeoutInMs);
                if (!success) {
                    logger.error(errorCode("FAILED_TO_STOP"), "failed to terminate kafka listener, name={}", name);
                } else {
                    logger.info("kafka listener stopped, uri={}, topics={}, name={}", uri, topics, name);
                }
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    String threadName(String name) {
        return "kafka-listener" + (name == null ? "" : "-" + name);
    }

    @SuppressWarnings("deprecation")
    Consumer<String, byte[]> createConsumer() {
        var watch = new StopWatch();
        try {
            Map<String, Object> config = Maps.newHashMapWithExpectedSize(13);
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri.bootstrapURIs);
            config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            config.put(ConsumerConfig.CLIENT_ID_CONFIG, Network.LOCAL_HOST_NAME + (name == null ? "" : "/" + name));      // will show in monitor metrics
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");                      // refer to org.apache.kafka.clients.consumer.ConsumerConfig, must be in("latest", "earliest", "none")
            config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 1_800_000);                  // 30min as max process time for each poll
            config.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 500L);                       // longer backoff to reduce cpu usage when kafka is not available
            config.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 5_000L);                 // 5s
            config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
            config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPollBytes);
            config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, minPollBytes);
            config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, (int) maxWaitTime.toMillis());
            config.put(ConsumerConfig.AUTO_INCLUDE_JMX_REPORTER_CONFIG, Boolean.FALSE);
            Consumer<String, byte[]> consumer = new KafkaConsumer<>(config, new KeyDeserializer(), new ByteArrayDeserializer());
            consumerMetrics.add(consumer.metrics());

            consumer.subscribe(topics);
            return consumer;
        } finally {
            logger.info("create kafka consumer, topics={}, name={}, elapsed={}", topics, name, watch.elapsed());
        }
    }
}
