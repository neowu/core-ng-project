package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;

/**
 * @author neo
 */
public interface RabbitMQ {
    void publish(String exchange, String routingKey, byte[] message, AMQP.BasicProperties properties);

    RabbitMQConsumer consumer(String queue, int prefetchCount);
}
