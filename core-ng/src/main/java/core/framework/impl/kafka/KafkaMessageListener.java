package core.framework.impl.kafka;

import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.MessageHandler;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.json.JSONReader;
import core.framework.impl.log.LogManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class KafkaMessageListener {
    final Map<String, MessageHandler<?>> handlers = Maps.newHashMap();
    final Map<String, BulkMessageHandler<?>> bulkHandlers = Maps.newHashMap();
    final Map<String, JSONReader<?>> readers = Maps.newHashMap();
    final Kafka kafka;
    final LogManager logManager;
    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final String name;
    private final Set<String> topics = Sets.newHashSet();
    public int poolSize = Runtime.getRuntime().availableProcessors() * 2;
    private KafkaMessageListenerThread[] listenerThreads;

    KafkaMessageListener(Kafka kafka, String name, LogManager logManager) {
        this.kafka = kafka;
        this.name = name;
        this.logManager = logManager;
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        if (topics.contains(topic)) throw Exceptions.error("topic is already subscribed, topic={}", topic);
        topics.add(topic);
        if (handler != null) handlers.put(topic, handler);
        if (bulkHandler != null) bulkHandlers.put(topic, bulkHandler);
        readers.put(topic, JSONReader.of(messageClass));
    }

    public void start() {
        listenerThreads = new KafkaMessageListenerThread[poolSize];
        String group = logManager.appName == null ? "local" : logManager.appName;
        for (int i = 0; i < poolSize; i++) {
            String name = "kafka-listener-" + (this.name == null ? "" : this.name + "-") + i;
            Consumer<String, byte[]> consumer = kafka.consumer(group, topics);
            KafkaMessageListenerThread thread = new KafkaMessageListenerThread(name, consumer, this);
            thread.start();
            listenerThreads[i] = thread;
        }
        logger.info("kafka listener started, name={}, uri={}, topics={}", name, kafka.uri, topics);
    }

    public void stop() {
        logger.info("stop kafka listener, name={}, uri={}, topics={}", name, kafka.uri, topics);
        for (KafkaMessageListenerThread thread : listenerThreads) {
            thread.shutdown();
        }
    }
}
