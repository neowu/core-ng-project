package core.framework.impl.kafka;

import core.framework.impl.json.JSONReader;
import core.framework.impl.log.LogManager;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class KafkaMessageListener {
    public final ConsumerMetrics consumerMetrics;
    final Map<String, MessageHandlerHolder<?>> handlerHolders = Maps.newHashMap();
    final Map<String, BulkMessageHandlerHolder<?>> bulkHandlerHolders = Maps.newHashMap();
    final LogManager logManager;

    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final String uri;
    private final String name;
    private final Set<String> topics = Sets.newHashSet();

    public int poolSize = Runtime.getRuntime().availableProcessors() * 4;
    public Duration maxProcessTime = Duration.ofMinutes(30);
    public int maxPollRecords = 500;            // default kafka setting, refer to org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
    public int maxPollBytes = 3 * 1024 * 1024;  // get 3M bytes message at max
    public int minPollBytes = 1;                // default kafka setting
    public Duration minPollMaxWaitTime = Duration.ofMillis(500);

    private KafkaMessageListenerThread[] listenerThreads;

    public KafkaMessageListener(String uri, String name, LogManager logManager) {
        this.uri = uri;
        this.name = name;
        this.logManager = logManager;
        this.consumerMetrics = new ConsumerMetrics(name);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        if (topics.contains(topic)) throw Exceptions.error("topic is already subscribed, topic={}", topic);
        topics.add(topic);
        MessageValidator<T> validator = new MessageValidator<>(messageClass);
        JSONReader<T> reader = JSONReader.of(messageClass);
        if (handler != null) handlerHolders.put(topic, new MessageHandlerHolder<>(handler, reader, validator));
        if (bulkHandler != null) bulkHandlerHolders.put(topic, new BulkMessageHandlerHolder<>(bulkHandler, reader, validator));
    }

    public void start() {
        listenerThreads = new KafkaMessageListenerThread[poolSize];
        String group = logManager.appName;
        for (int i = 0; i < poolSize; i++) {
            StopWatch watch = new StopWatch();
            String name = "kafka-listener-" + (this.name == null ? "" : this.name + "-") + i;
            Consumer<String, byte[]> consumer = consumer(group, topics);
            KafkaMessageListenerThread thread = new KafkaMessageListenerThread(name, consumer, this);
            thread.start();
            listenerThreads[i] = thread;
            logger.info("create kafka listener thread, name={}, topics={}, elapsedTime={}", name, topics, watch.elapsedTime());
        }
        logger.info("kafka listener started, uri={}, topics={}, name={}", uri, topics, name);
    }

    public void shutdown() {
        logger.info("shutting down kafka listener, uri={}, name={}", uri, name);
        if (listenerThreads != null) {
            for (KafkaMessageListenerThread thread : listenerThreads) {
                thread.shutdown();
            }
        }
    }

    public void awaitTermination(long timeoutInMs) {
        if (listenerThreads != null) {
            long end = System.currentTimeMillis() + timeoutInMs;
            for (KafkaMessageListenerThread thread : listenerThreads) {
                try {
                    thread.awaitTermination(end - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        logger.info("kafka listener stopped, uri={}, topics={}, name={}", uri, topics, name);
    }

    private Consumer<String, byte[]> consumer(String group, Set<String> topics) {
        if (uri == null) throw new Error("uri must not be null");
        Map<String, Object> config = Maps.newHashMap();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, (int) maxProcessTime.toMillis());
        config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) maxProcessTime.plusSeconds(5).toMillis());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        config.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, maxPollBytes);
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, minPollBytes);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, (int) minPollMaxWaitTime.toMillis());
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(config, new StringDeserializer(), new ByteArrayDeserializer());
        consumer.subscribe(topics);
        consumerMetrics.add(consumer.metrics());
        return consumer;
    }

    static class BulkMessageHandlerHolder<T> {
        final BulkMessageHandler<T> handler;
        final MessageValidator<T> validator;
        final JSONReader<T> reader;

        BulkMessageHandlerHolder(BulkMessageHandler<T> handler, JSONReader<T> reader, MessageValidator<T> validator) {
            this.handler = handler;
            this.validator = validator;
            this.reader = reader;
        }
    }

    static class MessageHandlerHolder<T> {
        final MessageHandler<T> handler;
        final MessageValidator<T> validator;
        final JSONReader<T> reader;

        MessageHandlerHolder(MessageHandler<T> handler, JSONReader<T> reader, MessageValidator<T> validator) {
            this.handler = handler;
            this.validator = validator;
            this.reader = reader;
        }
    }
}
