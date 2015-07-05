package core.framework.impl.queue;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
    Connection connection;

    public RabbitMQ() {
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setConnectionTimeout((int) Duration.ofSeconds(5).toMillis());
    }

    public RabbitMQ hosts(String... hosts) {
        logger.info("set rabbitmq client hosts, hosts={}", Arrays.toString(hosts));
        addresses = new Address[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String host = hosts[i];
            addresses[i] = new Address(host);
        }
        return this;
    }

    public void connectionTimeout(Duration timeout) {
        connectionFactory.setConnectionTimeout((int) timeout.toMillis());
    }

    public void shutdown() {
        if (connection != null) {
            logger.info("shutdown rabbitmq client, hosts={}", Arrays.toString(addresses));
            try {
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public Channel channel() {
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

    void closeChannel(Channel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new Error(e);
            }
        }
    }
}
