package core.framework.test.kafka;

import core.framework.kafka.MessagePublisher;
import core.framework.test.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author neo
 */
public class KafkaIntegrationTest extends IntegrationTest {
    @Inject
    MessagePublisher<TestMessage> publisher;

    @Test
    public void publish() {
        TestMessage message = new TestMessage();
        message.stringField = "value";
        publisher.publish("key", message);
    }
}
