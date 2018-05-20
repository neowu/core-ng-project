package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.kafka.Kafka;
import core.framework.impl.kafka.KafkaMessagePublisher;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
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
    private Kafka kafka;
    private boolean handlerAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        kafka = createKafka(context, name);
    }

    @Override
    protected void validate() {
        if (kafka.uri == null) throw Exceptions.error("kafka uri must be configured, name={}", name);
        if (!handlerAdded)
            throw Exceptions.error("kafka is configured, but no producer/consumer added, please remove unnecessary config, name={}", name);
    }

    Kafka createKafka(ModuleContext context, String name) {
        Kafka kafka = new Kafka(name, context.logManager);
        context.stat.metrics.add(kafka.producerMetrics);
        context.stat.metrics.add(kafka.consumerMetrics);
        context.startupHook.add(kafka::initialize);
        context.shutdownHook.add(kafka::close);

        KafkaController controller = new KafkaController(kafka);
        context.route(HTTPMethod.GET, managementPathPattern("/topic"), controller::topics, true);
        context.route(HTTPMethod.PUT, managementPathPattern("/topic/:topic"), controller::updateTopic, true);
        context.route(HTTPMethod.POST, managementPathPattern("/topic/:topic/message/:key"), controller::publish, true);

        return kafka;
    }

    String managementPathPattern(String postfix) {
        StringBuilder builder = new StringBuilder("/_sys/kafka");
        if (name != null) builder.append('/').append(name);
        builder.append(postfix);
        return builder.toString();
    }

    public <T> MessagePublisher<T> publish(Class<T> messageClass) {
        return publish(null, messageClass);
    }

    public <T> MessagePublisher<T> publish(String topic, Class<T> messageClass) {
        if (kafka.uri == null) throw Exceptions.error("kafka uri must be configured first, name={}", name);
        logger.info("create message publisher, topic={}, messageClass={}, name={}", topic, messageClass.getTypeName(), name);
        MessagePublisher<T> publisher = new KafkaMessagePublisher<>(kafka.producer(), topic, messageClass, context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), name, publisher);
        handlerAdded = true;
        return publisher;
    }

    public <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler) {
        subscribe(topic, messageClass, handler, null);
    }

    public <T> void subscribe(String topic, Class<T> messageClass, BulkMessageHandler<T> handler) {
        subscribe(topic, messageClass, null, handler);
    }

    private <T> void subscribe(String topic, Class<T> messageClass, MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler) {
        if (kafka.uri == null) throw Exceptions.error("kafka uri must be configured first, name={}", name);
        logger.info("subscribe topic, topic={}, messageClass={}, handlerClass={}, name={}", topic, messageClass.getTypeName(), handler != null ? handler.getClass().getCanonicalName() : bulkHandler.getClass().getCanonicalName(), name);
        kafka.listener().subscribe(topic, messageClass, handler, bulkHandler);
        handlerAdded = true;
    }

    public void poolSize(int poolSize) {
        kafka.listener().poolSize = poolSize;
    }

    public void uri(String uri) {
        if (kafka.uri != null)
            throw Exceptions.error("kafka uri is already configured, name={}, uri={}, previous={}", name, uri, kafka.uri);
        kafka.uri = uri;
    }

    public void maxProcessTime(Duration maxProcessTime) {
        kafka.maxProcessTime = maxProcessTime;
    }

    public void maxPoll(int maxRecords, int maxBytes) {
        if (maxRecords <= 0) throw Exceptions.error("max poll records must be greater than 0, value={}", maxRecords);
        if (maxBytes <= 0) throw Exceptions.error("max poll bytes must be greater than 0, value={}", maxBytes);
        kafka.maxPollRecords = maxRecords;
        kafka.maxPollBytes = maxBytes;
    }

    public void minPoll(int minBytes, Duration maxWaitTime) {
        if (minBytes <= 0) throw Exceptions.error("min poll bytes must be greater than 0, value={}", minBytes);
        if (maxWaitTime == null || maxWaitTime.toMillis() <= 0) throw Exceptions.error("max wait time must be greater than 0, value={}", maxWaitTime);
        kafka.minPollBytes = minBytes;
        kafka.minPollMaxWaitTime = maxWaitTime;
    }
}
