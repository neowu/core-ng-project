package core.framework.impl.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author neo
 */
public interface MessageProducer {
    void send(ProducerRecord<String, byte[]> record);
}
