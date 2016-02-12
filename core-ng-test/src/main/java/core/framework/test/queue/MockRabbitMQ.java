package core.framework.test.queue;

import com.rabbitmq.client.AMQP;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQConsumer;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author neo
 */
public class MockRabbitMQ implements RabbitMQ {
    private final Map<String, Queue<byte[]>> publishedMessages = new ConcurrentHashMap<>();

    @Override
    public void publish(String exchange, String routingKey, byte[] message, AMQP.BasicProperties properties) {
        Queue<byte[]> queue = publishedMessages.computeIfAbsent(exchange + ":" + routingKey, key -> new ConcurrentLinkedQueue<>());
        queue.add(message);
    }

    @Override
    public RabbitMQConsumer consumer(String queue, int prefetchCount) {
        throw new Error("not supported");
    }

    public Queue<byte[]> publishedMessages(String exchange, String routingKey) {
        return publishedMessages.remove(exchange + ":" + routingKey);
    }
}
