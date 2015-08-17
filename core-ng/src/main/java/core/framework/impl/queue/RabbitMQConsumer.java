package core.framework.impl.queue;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeoutException;

/**
 * @author neo
 */
public class RabbitMQConsumer implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final String queue;
    private final Channel channel;
    private final QueueingConsumer consumer;

    public RabbitMQConsumer(Channel channel, String queue, int prefetchCount) {
        this.channel = channel;
        this.queue = queue;
        consumer = new QueueingConsumer(channel);
        try {
            channel.basicQos(prefetchCount);
            channel.basicConsume(queue, false, consumer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public QueueingConsumer.Delivery nextDelivery() {
        try {
            return consumer.nextDelivery();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public void acknowledge(long deliveryTag) {
        StopWatch watch = new StopWatch();
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException | AlreadyClosedException e) {
            logger.error("failed to acknowledge message due to network issue, rabbitMQ will resend message if connection is closed", e);
            throw new Error(e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("acknowledge, queue={}, deliveryTag={}, elapsedTime={}", queue, deliveryTag, elapsedTime);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException | TimeoutException e) {
            throw new Error(e);
        }
    }
}
