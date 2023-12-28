package core.framework.module;

import core.framework.internal.kafka.MockMessagePublisher;
import core.framework.kafka.MessagePublisher;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestKafkaConfig extends KafkaConfig {
    @Override
    <T> MessagePublisher<T> createMessagePublisher(String topic, Class<T> messageClass) {
        // create custom mock message publisher to do validation check on integration test
        // as well as leveraging mockito feature on test
        return Mockito.spy(new MockMessagePublisher<>(messageClass));
    }
}
