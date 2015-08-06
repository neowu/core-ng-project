package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * @author neo
 */
public class RabbitMQ {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQ.class);
    public final ConnectionFactory connectionFactory = new ConnectionFactory();
    private Address[] addresses;
    private Connection connection;

    public RabbitMQ() {
        connectionFactory.setAutomaticRecoveryEnabled(true);
        timeout(Duration.ofSeconds(5));
        connectionFactory.setUsername("rabbitmq");  // default user/password
        connectionFactory.setPassword("rabbitmq");
    }

    public void shutdown() {
        if (connection != null) {
            logger.info("close rabbitMQ connection, hosts={}", Arrays.toString(addresses));
            try {
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public void hosts(String... hosts) {
        logger.info("set rabbitMQ hosts, hosts={}", Arrays.toString(hosts));
        addresses = new Address[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String host = hosts[i];
            addresses[i] = new Address(host);
        }
    }

    public void timeout(Duration timeout) {
        connectionFactory.setConnectionTimeout((int) timeout.toMillis());
    }

    public RabbitMQConsumer consumer(String queue, int prefetchCount) {
        Channel channel = channel();
        return new RabbitMQConsumer(channel, queue, prefetchCount);
    }

    public void publish(String exchange, String routingKey, String message, AMQP.BasicProperties properties) {
        StopWatch watch = new StopWatch();
        Channel channel = null;
        try {
            channel = channel();
            channel.basicPublish(exchange, routingKey, properties, Strings.bytes(message));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            closeChannel(channel);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("publish, exchange={}, routingKey={}, messageId={}, elapsedTime={}", exchange, routingKey, properties.getMessageId(), elapsedTime);
        }
    }

    private Channel channel() {
        try {
            synchronized (this) {
                if (connection == null) {
                    connection = connectionFactory.newConnection(addresses);
                }
            }
            return connection.createChannel();
        } catch (IOException | TimeoutException e) {
            throw new Error(e);
        }
    }

    private void closeChannel(Channel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new Error(e);
            }
        }
    }
}
