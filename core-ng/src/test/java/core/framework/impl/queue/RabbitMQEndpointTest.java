package core.framework.impl.queue;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class RabbitMQEndpointTest {
    @Test
    public void exchange() {
        RabbitMQEndpoint destination = new RabbitMQEndpoint("rabbitmq://exchange/test");
        Assert.assertEquals("test", destination.exchange);
        Assert.assertEquals("", destination.routingKey);
    }

    @Test
    public void defaultExchange() {
        // in rabbitmq default exchange is direct and route message to queueName=routingKey
        RabbitMQEndpoint destination = new RabbitMQEndpoint("rabbitmq://exchange/");
        Assert.assertEquals("", destination.exchange);
        Assert.assertEquals("", destination.routingKey);
    }

    @Test
    public void queue() {
        RabbitMQEndpoint destination = new RabbitMQEndpoint("rabbitmq://queue/test");
        Assert.assertEquals("", destination.exchange);
        Assert.assertEquals("test", destination.routingKey);
    }
}