package core.framework.test.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.MessagePublisher;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        publisher.publish("topic1", "key", message);

        verify(publisher).publish(eq("topic1"), eq("key"), argThat(arg -> "value".equals(arg.stringField)));
    }

    @Test
    void publishWithNullTopic() {
        assertThatThrownBy(() -> publisher.publish(null, null, new TestMessage()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("topic must not be null");
    }
}
