package core.framework.impl.kafka;

import core.framework.impl.json.JSONWriter;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.filter.BytesParam;
import core.framework.kafka.MessagePublisher;
import core.framework.log.ActionLogContext;
import core.framework.util.Network;
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
    private final LogManager logManager;
    private final JSONWriter<T> writer;

    public MessagePublisherImpl(MessageProducer producer, String topic, Class<T> messageClass, LogManager logManager) {
        this.producer = producer;
        this.topic = topic;
        this.logManager = logManager;
        this.validator = new MessageValidator<>(messageClass);
        writer = JSONWriter.of(messageClass);
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
        byte[] message = writer.toJSON(value);
        try {
            var record = new ProducerRecord<>(topic, key, message);
            linkContext(record.headers());
            producer.send(record);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("kafka", elapsed, 0, 1);   // kafka producer send message in background, the main purpose of track is to count how many message sent in action
            logger.debug("publish, topic={}, key={}, message={}, elapsed={}", topic, key, new BytesParam(message), elapsed);
        }
    }

    private void linkContext(Headers headers) {
        headers.add(MessageHeaders.HEADER_CLIENT_IP, Strings.bytes(Network.localHostAddress()));
        headers.add(MessageHeaders.HEADER_CLIENT, Strings.bytes(logManager.appName));

        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return;

        headers.add(MessageHeaders.HEADER_REF_ID, Strings.bytes(actionLog.refId()));
        if (actionLog.trace) headers.add(MessageHeaders.HEADER_TRACE, Strings.bytes("true"));
    }
}
