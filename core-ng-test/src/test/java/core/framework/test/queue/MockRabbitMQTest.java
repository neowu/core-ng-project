package core.framework.test.queue;

import core.framework.api.util.Charsets;
import core.framework.api.util.Strings;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class MockRabbitMQTest {
    MockRabbitMQ rabbitMQ;

    @Before
    public void createMockRabbitMQ() {
        rabbitMQ = new MockRabbitMQ();
    }

    @Test
    public void publish() {
        Queue<byte[]> messages = rabbitMQ.publishedMessages("", "queue");
        assertNull(messages);

        rabbitMQ.publish("", "queue", Strings.bytes("message"), null);
        messages = rabbitMQ.publishedMessages("", "queue");
        assertEquals(1, messages.size());
        assertEquals("message", new String(messages.poll(), Charsets.UTF_8));
    }
}