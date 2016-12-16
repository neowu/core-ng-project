package core.framework.api.module;

import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.MessageHandler;
import core.framework.api.kafka.MessagePublisher;
import core.framework.api.util.Types;
import core.framework.impl.kafka.Kafka;
import core.framework.impl.kafka.KafkaListener;
import core.framework.impl.kafka.KafkaPublisher;
import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class KafkaConfig {
    private final ModuleContext context;
    private final Kafka kafka;
    private final String name;

    public KafkaConfig(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        if (context.beanFactory.registered(Kafka.class, name)) {
            kafka = context.beanFactory.bean(Kafka.class, name);
        } else {
            if (context.isTest()) {
                kafka = context.mockFactory.create(Kafka.class);
            } else {
                Kafka kafka = new Kafka();
                context.startupHook.add(kafka::initialize);
                context.shutdownHook.add(kafka::close);
                this.kafka = kafka;
            }
            context.beanFactory.bind(Kafka.class, name, kafka);
        }
    }

    public <T> void publish(String topic, Class<T> messageClass) {
        kafka.validator.register(messageClass);
        MessagePublisher<T> publisher = new KafkaPublisher<>(kafka.producer(), kafka.validator, topic, messageClass, context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler) {
        kafka.validator.register(messageClass);
        listener().subscribe(topic, messageClass, handler, null);
        return this;
    }

    public <T> KafkaConfig subscribe(String topic, Class<T> messageClass, BulkMessageHandler<T> handler) {
        kafka.validator.register(messageClass);
        listener().subscribe(topic, messageClass, null, handler);
        return this;
    }

    public void poolSize(int poolSize) {
        listener().poolSize = poolSize;
    }

    public void uri(String uri) {
        if (!context.isTest()) {
            kafka.uri = uri;
        }
    }

    private KafkaListener listener() {
        if (kafka.listener == null) {
            kafka.listener = new KafkaListener(kafka, name, context.logManager);
        }
        return kafka.listener;
    }
}
