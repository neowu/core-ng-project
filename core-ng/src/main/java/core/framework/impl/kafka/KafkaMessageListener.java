package core.framework.impl.kafka;

import core.framework.impl.json.JSONReader;
import core.framework.impl.log.LogManager;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Sets;
import core.framework.util.Threads;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class KafkaMessageListener {
    final Map<String, MessageHandlerHolder<?>> handlerHolders = Maps.newHashMap();
    final Map<String, BulkMessageHandlerHolder<?>> bulkHandlerHolders = Maps.newHashMap();
    final Kafka kafka;
    final LogManager logManager;
    private final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final String name;
    private final Set<String> topics = Sets.newHashSet();
    public int poolSize = Threads.availableProcessors() * 4;
    private KafkaMessageListenerThread[] listenerThreads;

    KafkaMessageListener(Kafka kafka, String name, LogManager logManager) {
        this.kafka = kafka;
        this.name = name;
        this.logManager = logManager;
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
        if (listenerThreads != null) {
            for (KafkaMessageListenerThread thread : listenerThreads) {
                thread.shutdown();
            }
        }
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
