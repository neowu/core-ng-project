package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;

/**
 * @author neo
 */
public interface RabbitMQ {
    void publish(String exchange, String routingKey, byte[] message, AMQP.BasicProperties properties);

    // consumer must be used in current thread, since the impl uses LockSupport.park()
    RabbitMQConsumer consumer(String queue, int prefetchCount);
}
