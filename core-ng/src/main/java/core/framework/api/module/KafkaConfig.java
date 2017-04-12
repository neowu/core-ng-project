package core.framework.api.module;

import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.MessageHandler;
import core.framework.api.kafka.MessagePublisher;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Types;
import core.framework.impl.kafka.Kafka;
import core.framework.impl.kafka.KafkaMessagePublisher;
import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class KafkaConfig {
    private final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    private final ModuleContext context;
    private final String name;
    private final State state;

    public KafkaConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.kafka(name);

        if (state.kafka == null) {
            state.kafka = createKafka(context, name);
        }
    }

    private Kafka createKafka(ModuleContext context, String name) {
        if (context.isTest()) {
            return context.mockFactory.create(Kafka.class);
        } else {
            Kafka kafka = new Kafka(name, context.logManager);
            context.metrics.add(kafka.producerMetrics);
            context.metrics.add(kafka.consumerMetrics);
            context.startupHook.add(kafka::initialize);
            context.shutdownHook.add(kafka::close);
            return kafka;
        }
    }

    public <T> MessagePublisher<T> publish(String topic, Class<T> messageClass) {
        if (state.kafka.uri == null) throw Exceptions.error("kafka({}).uri() must be configured first", name == null ? "" : name);
        logger.info("create message publisher, topic={}, messageClass={}, beanName={}", topic, messageClass.getTypeName(), name);
        state.kafka.validator.register(messageClass);
        MessagePublisher<T> publisher = new KafkaMessagePublisher<>(state.kafka.producer(), state.kafka.validator, topic, messageClass, context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
        state.handlerAdded = true;
        return publisher;
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler) {
        return subscribe(topic, messageClass, handler, null);
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, BulkMessageHandler<T> handler) {
        return subscribe(topic, messageClass, null, handler);
    }

    private <T> KafkaConfig subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        if (state.kafka.uri == null) throw Exceptions.error("kafka({}).uri() must be configured first", name == null ? "" : name);
        state.kafka.validator.register(messageClass);
        state.kafka.listener().subscribe(topic, messageClass, handler, bulkHandler);
        state.handlerAdded = true;
        return this;
    }

    public void poolSize(int poolSize) {
        state.kafka.listener().poolSize = poolSize;
    }

    public void uri(String uri) {
        if (state.kafka.uri != null)
            throw Exceptions.error("kafka({}).uri() is already configured, uri={}, previous={}", name == null ? "" : name, uri, state.kafka.uri);
        state.kafka.uri = uri;
    }

    public void maxProcessTime(Duration maxProcessTime) {
        state.kafka.maxProcessTime = maxProcessTime;
    }

    public void maxPoll(int maxRecords, int maxBytes) {
        if (maxRecords <= 0) throw Exceptions.error("max poll records must be greater than 0, value={}", maxRecords);
        if (maxBytes <= 0) throw Exceptions.error("max poll bytes must be greater than 0, value={}", maxBytes);
        state.kafka.maxPollRecords = maxRecords;
        state.kafka.maxPollBytes = maxBytes;
    }

    public void minPoll(int minBytes, Duration maxWaitTime) {
        if (minBytes <= 0) throw Exceptions.error("min poll bytes must be greater than 0, value={}", minBytes);
        if (maxWaitTime == null || maxWaitTime.toMillis() <= 0) throw Exceptions.error("max wait time must be greater than 0, value={}", maxWaitTime);
        state.kafka.minPollBytes = minBytes;
        state.kafka.minPollMaxWaitTime = maxWaitTime;
    }

    public static class State {
        final String name;
        Kafka kafka;
        boolean handlerAdded;

        public State(String name) {
            this.name = name;
        }

        public void validate() {
            if (kafka.uri == null) throw Exceptions.error("kafka({}).uri() must be configured", name == null ? "" : name);
            if (!handlerAdded)
                throw Exceptions.error("kafka({}) is configured, but no producer/consumer added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
