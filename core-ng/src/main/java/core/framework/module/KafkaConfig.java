package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.kafka.MessageListener;
import core.framework.internal.kafka.MessageProducer;
import core.framework.internal.kafka.MessagePublisherImpl;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.web.management.KafkaController;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.kafka.MessagePublisher;
import core.framework.util.Types;
import org.apache.kafka.common.record.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class KafkaConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);
    String name;
    MessageProducer producer;
    private ModuleContext context;
    private String uri;
    private MessageListener listener;
    private boolean handlerAdded;
    // refer to org.apache.kafka.clients.producer.KafkaProducer.ensureValidRecordSize,
    // by default kafka producer max.request.size is 1 * 1024 * 1024=1048576,
    // but the default max.message.bytes on broker=1000012 (core/src/main/scala/kafka/server/KafkaConfig.scala, val MessageMaxBytes = 1000000 + Records.LOG_OVERHEAD)
    // if producer may send record larger than max.message.bytes and less than max.request.size, and it won't trigger any error, and broker silently drops record
    // so here it changes the default to match broker default (which is flaw of kafka default config)
    private int maxRequestSize = 1000000 + Records.LOG_OVERHEAD;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
    }

    @Override
    protected void validate() {
        if (!handlerAdded)
            throw new Error("kafka is configured, but no producer/consumer added, please remove unnecessary config, name=" + name);
    }

    public void uri(String uri) {
        if (this.uri != null)
            throw new Error(format("kafka uri is already configured, name={}, uri={}, previous={}", name, uri, this.uri));
        this.uri = uri;
    }

    // for use case as replying message back to publisher, so the topic can be dynamic (different services (consumer group) expect to receive reply in their own topic)
    public <T> MessagePublisher<T> publish(Class<T> messageClass) {
        return publish(null, messageClass);
    }

    public <T> MessagePublisher<T> publish(String topic, Class<T> messageClass) {
        logger.info("publish, topic={}, messageClass={}, name={}", topic, messageClass.getTypeName(), name);
        if (uri == null) throw new Error("kafka uri must be configured first, name=" + name);
        new BeanClassValidator(messageClass, context.serviceRegistry.beanClassNameValidator).validate();
        MessagePublisher<T> publisher = createMessagePublisher(topic, messageClass);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
        handlerAdded = true;
        return publisher;
    }

    // to increase max message size, it must change on both producer and broker sides
    public void maxRequestSize(int size) {
        if (size <= 0) throw new Error("max request size must be greater than 0, value=" + size);
        if (producer != null) throw new Error("kafka().maxRequestSize() must be configured before adding publisher");
        maxRequestSize = size;
    }

    <T> MessagePublisher<T> createMessagePublisher(String topic, Class<T> messageClass) {
        if (producer == null) {
            producer = createMessageProducer();
        }
        return new MessagePublisherImpl<>(producer, topic, messageClass);
    }

    private MessageProducer createMessageProducer() {
        var producer = new MessageProducer(uri, name, maxRequestSize);
        context.collector.metrics.add(producer.producerMetrics);
        context.shutdownHook.add(ShutdownHook.STAGE_4, producer::close);
        var controller = new KafkaController(producer);
        context.route(HTTPMethod.POST, managementPathPattern("/topic/:topic/message/:key"), (LambdaController) controller::publish, true);
        return producer;
    }

    String managementPathPattern(String postfix) {
        var builder = new StringBuilder("/_sys/kafka");
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
        if (handler == null && bulkHandler == null) throw new Error("handler must not be null");
        logger.info("subscribe, topic={}, messageClass={}, handlerClass={}, name={}", topic, messageClass.getTypeName(), handler != null ? handler.getClass().getCanonicalName() : bulkHandler.getClass().getCanonicalName(), name);
        new BeanClassValidator(messageClass, context.serviceRegistry.beanClassNameValidator).validate();
        listener().subscribe(topic, messageClass, handler, bulkHandler);
        handlerAdded = true;
    }

    private MessageListener listener() {
        if (listener == null) {
            if (uri == null) throw new Error("kafka uri must be configured first, name=" + name);
            listener = new MessageListener(uri, name, context.logManager);
            context.startupHook.add(listener::start);
            context.shutdownHook.add(ShutdownHook.STAGE_0, timeout -> listener.shutdown());
            context.shutdownHook.add(ShutdownHook.STAGE_1, listener::awaitTermination);
            context.collector.metrics.add(listener.consumerMetrics);
        }
        return listener;
    }

    // by default listener use AppName as consumer group
    // e.g. use Network.LOCAL_HOST_NAME to make every pod receives messages from topic, (local cache invalidation, web socket notification)
    // use "${service-name}-${label}" to allow same service to be deployed for mutlitenancy
    public void groupId(String groupId) {
        listener().groupId = groupId;
    }

    public void poolSize(int poolSize) {
        listener().poolSize = poolSize;
    }

    public void maxProcessTime(Duration maxProcessTime) {
        listener().maxProcessTime = maxProcessTime;
    }

    public void longConsumerLagThreshold(Duration threshold) {
        listener().longConsumerLagThreshold = threshold;
    }

    public void maxPoll(int maxRecords, int maxBytes) {
        if (maxRecords <= 0) throw new Error("max poll records must be greater than 0, value=" + maxRecords);
        if (maxBytes <= 0) throw new Error("max poll bytes must be greater than 0, value=" + maxBytes);
        MessageListener listener = listener();
        listener.maxPollRecords = maxRecords;
        listener.maxPollBytes = maxBytes;
    }

    public void minPoll(int minBytes, Duration maxWaitTime) {
        if (minBytes <= 0) throw new Error("min poll bytes must be greater than 0, value=" + minBytes);
        if (maxWaitTime == null || maxWaitTime.toMillis() <= 0) throw new Error("max wait time must be greater than 0, value=" + maxWaitTime);
        MessageListener listener = listener();
        listener.minPollBytes = minBytes;
        listener.maxWaitTime = maxWaitTime;
    }
}
