package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeoutException;

/**
 * @author neo
 */
public class RabbitMQChannel implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQChannel.class);
    private final Channel channel;

    public RabbitMQChannel(Channel channel) {
        this.channel = channel;
    }

    public void publish(String exchange, String routingKey, String message, AMQP.BasicProperties properties) {
        StopWatch watch = new StopWatch();
        try {
            channel.basicPublish(exchange, routingKey, properties, Strings.bytes(message));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("publish, exchange={}, routingKey={}, elapsedTime={}", exchange, routingKey, elapsedTime);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (TimeoutException | IOException e) {
            throw new Error(e);
        }
    }
}
