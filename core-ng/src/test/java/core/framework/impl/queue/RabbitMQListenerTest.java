package core.framework.impl.queue;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RabbitMQListenerTest {
    RabbitMQListener rabbitMQListener;

    @Before
    public void createRabbitMQListener() {
        rabbitMQListener = new RabbitMQListener(null, "test-queue", null, null);
    }

    @Test
    public void action() {
        assertEquals("queue/test-queue/message_type", rabbitMQListener.action("message_type"));

        assertEquals("queue/test-queue", rabbitMQListener.action(null));
    }
}