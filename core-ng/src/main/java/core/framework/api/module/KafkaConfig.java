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
    private final KafkaConfigState state;

    public KafkaConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        state = context.config.kafka(name);

        if (state.kafka == null) {
            if (context.isTest()) {
                state.kafka = context.mockFactory.create(Kafka.class);
            } else {
                Kafka kafka = new Kafka(name, context.logManager);
                context.metrics.add(kafka.producerMetrics);
                context.metrics.add(kafka.consumerMetrics);
                context.startupHook.add(kafka::initialize);
                context.shutdownHook.add(kafka::close);
                state.kafka = kafka;
            }
        }
    }

    public <T> void publish(String topic, Class<T> messageClass) {
        logger.info("create message publisher, topic={}, messageClass={}, beanName={}", topic, messageClass.getTypeName(), name);
        state.kafka.validator.register(messageClass);
        MessagePublisher<T> publisher = new KafkaMessagePublisher<>(state.kafka.producer(), state.kafka.validator, topic, messageClass, context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
        state.producerAdded = true;
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler) {
        state.kafka.validator.register(messageClass);
        state.kafka.listener().subscribe(topic, messageClass, handler, null);
        state.consumerAdded = true;
        return this;
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, BulkMessageHandler<T> handler) {
        state.kafka.validator.register(messageClass);
        state.kafka.listener().subscribe(topic, messageClass, null, handler);
        state.consumerAdded = true;
        return this;
    }

    public void poolSize(int poolSize) {
        state.kafka.listener().poolSize = poolSize;
    }

    public void uri(String uri) {
        state.kafka.uri = uri;
        state.uri = uri;
    }

    public void maxProcessTime(Duration timeout) {
        state.kafka.maxProcessTime = timeout;
    }

    public static class KafkaConfigState {
        final String name;
        Kafka kafka;
        String uri;
        boolean producerAdded;
        boolean consumerAdded;

        public KafkaConfigState(String name) {
            this.name = name;
        }

        public void validate() {
            if (uri == null) throw Exceptions.error("kafka({}).uri() must be configured", name == null ? "" : name);
            if (!producerAdded && !consumerAdded)
                throw Exceptions.error("kafka({}) is configured, but no producer or consumer added, please remove unnecessary config", name == null ? "" : name);
        }
    }
}
