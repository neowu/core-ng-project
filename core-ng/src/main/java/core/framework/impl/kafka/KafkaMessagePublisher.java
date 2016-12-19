package core.framework.impl.kafka;

import core.framework.api.kafka.MessagePublisher;
import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.api.util.Types;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

/**
 * @author neo
 */
public class KafkaMessagePublisher<T> implements MessagePublisher<T> {
    private final Producer<String, byte[]> producer;
    private final MessageValidator validator;
    private final String topic;
    private final LogManager logManager;
    private final JSONWriter<KafkaMessage<T>> writer;

    public KafkaMessagePublisher(Producer<String, byte[]> producer, MessageValidator validator, String topic, Class<T> messageClass, LogManager logManager) {
        this.producer = producer;
        this.validator = validator;
        this.topic = topic;
        this.logManager = logManager;
        writer = JSONWriter.of(Types.generic(KafkaMessage.class, messageClass));
    }

    @Override
    public void publish(String key, T value) {
        publish(topic, key, value);
    }

    @Override
    public void publish(String topic, String key, T value) {
        validator.validate(value);

        KafkaMessage<T> kafkaMessage = new KafkaMessage<>();
        Map<String, String> headers = Maps.newHashMap();
        headers.put(KafkaMessage.HEADER_CLIENT_IP, Network.localHostAddress());
        headers.put(KafkaMessage.HEADER_CLIENT, logManager.appName);
        linkContext(headers);
        kafkaMessage.headers = headers;
        kafkaMessage.value = value;
        byte[] bytes = writer.toJSON(kafkaMessage);

        producer.send(new ProducerRecord<>(topic, key, bytes));
    }

    private void linkContext(Map<String, String> headers) {
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return;

        headers.put(KafkaMessage.HEADER_REF_ID, actionLog.refId());
        if (actionLog.trace) headers.put(KafkaMessage.HEADER_TRACE, "true");
    }
}
