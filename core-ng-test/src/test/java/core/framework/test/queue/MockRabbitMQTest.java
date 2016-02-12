package core.framework.test.queue;

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
        Queue<String> messages = rabbitMQ.publishedMessages("", "queue");
        assertNull(messages);

        rabbitMQ.publish("", "queue", "message", null);
        messages = rabbitMQ.publishedMessages("", "queue");
        assertEquals(1, messages.size());
        assertEquals("message", messages.poll());
    }
}