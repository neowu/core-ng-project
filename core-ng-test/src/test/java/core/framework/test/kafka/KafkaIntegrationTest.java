package core.framework.test.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.MessagePublisher;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class KafkaIntegrationTest extends IntegrationTest {
    @Inject
    MessagePublisher<TestMessage> publisher;

    @Test
    void publish() {
        TestMessage message = new TestMessage();
        message.stringField = "value";
        publisher.publish("key", message);
    }
}
