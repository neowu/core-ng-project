package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
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
    public final Pool<Channel> channelPool;

    public RabbitMQ() {
        connectionFactory.setAutomaticRecoveryEnabled(true);
        timeout(Duration.ofSeconds(5));
        connectionFactory.setUsername("rabbitmq");  // default user/password
        connectionFactory.setPassword("rabbitmq");
        channelPool = new Pool<>(this::createChannel, Channel::close);
        channelPool.name("rabbitmq");
        channelPool.poolSize(1, 5);
        channelPool.maxIdleTime(Duration.ofMinutes(30));
    }

    public void shutdown() {
        if (connection != null) {
            logger.info("shutdown rabbitMQ client, hosts={}", Arrays.toString(addresses));
            try {
                channelPool.close();
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public void hosts(String... hosts) {
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
        Channel channel = createChannel();
        return new RabbitMQConsumer(channel, queue, prefetchCount);
    }

    public void publish(String exchange, String routingKey, String message, AMQP.BasicProperties properties) {
        StopWatch watch = new StopWatch();
        PoolItem<Channel> item = channelPool.borrowItem();
        try {
            item.resource.basicPublish(exchange, routingKey, properties, Strings.bytes(message));
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            channelPool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("publish, exchange={}, routingKey={}, elapsedTime={}", exchange, routingKey, elapsedTime);
        }
    }

    public Channel createChannel() {
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
}
