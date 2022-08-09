package core.framework.test.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.MessagePublisher;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class MessagePublisherIntegrationTest extends IntegrationTest {
    @Inject
    MessagePublisher<TestMessage> publisher;

    @Test
    void publish() {
        var message = new TestMessage();
        message.stringField = "value";
        publisher.publish("key", message);

        verify(publisher).publish(eq("key"), argThat(arg -> "value".equals(arg.stringField)));
    }
}
