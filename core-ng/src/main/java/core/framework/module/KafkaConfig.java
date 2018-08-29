package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.kafka.MessageListener;
import core.framework.impl.kafka.MessageProducer;
import core.framework.impl.kafka.MessageProducerImpl;
import core.framework.impl.kafka.MessagePublisherImpl;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.impl.web.management.KafkaController;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.kafka.MessagePublisher;
import core.framework.util.Exceptions;
import core.framework.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class KafkaConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);
    String name;
    private ModuleContext context;
    private String uri;
    private MessageProducer producer;
    private MessageListener listener;
    private boolean handlerAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
    }

    @Override
    protected void validate() {
        if (uri == null) throw Exceptions.error("kafka uri must be configured, name={}", name);
        if (!handlerAdded)
            throw Exceptions.error("kafka is configured, but no producer/consumer added, please remove unnecessary config, name={}", name);
    }

    public void uri(String uri) {
        if (this.uri != null)
            throw Exceptions.error("kafka uri is already configured, name={}, uri={}, previous={}", name, uri, this.uri);
        this.uri = uri;
    }

    public <T> MessagePublisher<T> publish(Class<T> messageClass) {
        return publish(null, messageClass);
    }

    public <T> MessagePublisher<T> publish(String topic, Class<T> messageClass) {
        logger.info("create message publisher, topic={}, messageClass={}, name={}", topic, messageClass.getTypeName(), name);
        var publisher = new MessagePublisherImpl<>(producer(), topic, messageClass, context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
        handlerAdded = true;
        return publisher;
    }

    private MessageProducer producer() {
        if (producer == null) {
            if (uri == null) throw Exceptions.error("kafka uri must be configured first, name={}", name);
            this.producer = createProducer();
        }
        return producer;
    }

    MessageProducer createProducer() {
        var producer = new MessageProducerImpl(uri, name);
        context.stat.metrics.add(producer.producerMetrics);
        context.shutdownHook.add(ShutdownHook.STAGE_3, producer::close);
        var controller = new KafkaController(producer);
        context.route(HTTPMethod.POST, managementPathPattern("/topic/:topic/message/:key"), controller::publish, true);
        return producer;
    }

    String managementPathPattern(String postfix) {
        StringBuilder builder = new StringBuilder("/_sys/kafka");
        if (name != null) builder.append('/').append(name);
        builder.append(postfix);
        return builder.toString();
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler) {
        subscribe(topic, messageClass, handler, null);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, BulkMessageHandler<T> handler) {
        subscribe(topic, messageClass, null, handler);
    }

    private <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        logger.info("subscribe topic, topic={}, messageClass={}, handlerClass={}, name={}", topic, messageClass.getTypeName(), handler != null ? handler.getClass().getCanonicalName() : bulkHandler.getClass().getCanonicalName(), name);
        listener().subscribe(topic, messageClass, handler, bulkHandler);
        handlerAdded = true;
    }

    private MessageListener listener() {
        if (listener == null) {
            if (uri == null) throw Exceptions.error("kafka uri must be configured first, name={}", name);
            listener = new MessageListener(uri, name, context.logManager);
            context.startupHook.add(listener::start);
            context.shutdownHook.add(ShutdownHook.STAGE_0, timeout -> listener.shutdown());
            context.shutdownHook.add(ShutdownHook.STAGE_1, listener::awaitTermination);
            context.stat.metrics.add(listener.consumerMetrics);
        }
        return listener;
    }

    public void poolSize(int poolSize) {
        listener().poolSize = poolSize;
    }

    public void maxProcessTime(Duration maxProcessTime) {
        listener().maxProcessTime = maxProcessTime;
    }

    public void maxPoll(int maxRecords, int maxBytes) {
        if (maxRecords <= 0) throw Exceptions.error("max poll records must be greater than 0, value={}", maxRecords);
        if (maxBytes <= 0) throw Exceptions.error("max poll bytes must be greater than 0, value={}", maxBytes);
        MessageListener listener = listener();
        listener.maxPollRecords = maxRecords;
        listener.maxPollBytes = maxBytes;
    }

    public void minPoll(int minBytes, Duration maxWaitTime) {
        if (minBytes <= 0) throw Exceptions.error("min poll bytes must be greater than 0, value={}", minBytes);
        if (maxWaitTime == null || maxWaitTime.toMillis() <= 0) throw Exceptions.error("max wait time must be greater than 0, value={}", maxWaitTime);
        MessageListener listener = listener();
        listener.minPollBytes = minBytes;
        listener.minPollMaxWaitTime = maxWaitTime;
    }
}
