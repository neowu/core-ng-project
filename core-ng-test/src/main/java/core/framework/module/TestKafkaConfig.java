package core.framework.module;

import core.framework.kafka.MessagePublisher;

import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
public class TestKafkaConfig extends KafkaConfig {
    @SuppressWarnings("unchecked")
    @Override
    <T> MessagePublisher<T> createMessagePublisher(String topic, Class<T> messageClass) {
        return (MessagePublisher<T>) mock(MessagePublisher.class);
    }
}
