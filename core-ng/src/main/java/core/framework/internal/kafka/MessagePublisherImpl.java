package core.framework.internal.kafka;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.filter.BytesLogParam;
import core.framework.internal.json.JSONMapper;
import core.framework.kafka.MessagePublisher;
import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class MessagePublisherImpl<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(MessagePublisherImpl.class);

    private final MessageProducer producer;
    private final MessageValidator<T> validator;
    private final String topic;
    private final JSONMapper<T> mapper;

    public MessagePublisherImpl(MessageProducer producer, String topic, Class<T> messageClass) {
        this.producer = producer;
        this.topic = topic;
        this.validator = new MessageValidator<>(messageClass);
        mapper = new JSONMapper<>(messageClass);
    }

    @Override
    public void publish(String key, T value) {
        publish(topic, key, value);
    }

    @Override
    public void publish(String topic, String key, T value) {
        if (topic == null) throw new Error("topic must not be null");
        if (key == null) throw new Error("key must not be null");   // if key is null, kafka will pick random partition which breaks determinacy

        var watch = new StopWatch();
        validator.validate(value);
        byte[] message = mapper.toJSON(value);
        try {
            var record = new ProducerRecord<>(topic, Strings.bytes(key), message);
            linkContext(record.headers());
            producer.send(record);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("kafka", elapsed, 0, 1);   // kafka producer send message in background, the main purpose of track is to count how many message sent in action
            logger.debug("publish, topic={}, key={}, message={}, elapsed={}", topic, key, new BytesLogParam(message), elapsed);
        }
    }

    private void linkContext(Headers headers) {
        headers.add(MessageHeaders.HEADER_CLIENT, Strings.bytes(LogManager.APP_NAME));

        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return;      // publisher may be used without action log context

        headers.add(MessageHeaders.HEADER_CORRELATION_ID, Strings.bytes(actionLog.correlationId()));
        if (actionLog.trace) headers.add(MessageHeaders.HEADER_TRACE, Strings.bytes("true"));
        headers.add(MessageHeaders.HEADER_REF_ID, Strings.bytes(actionLog.id));
    }
}
