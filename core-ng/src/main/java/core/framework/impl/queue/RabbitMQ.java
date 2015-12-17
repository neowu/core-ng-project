package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public final class RabbitMQ {
    public final Pool<Channel> pool;
    private final Logger logger = LoggerFactory.getLogger(RabbitMQ.class);
    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    private final Lock lock = new ReentrantLock();
    private Address[] addresses;
    private long slowMessageThresholdInMs = 100;
    private volatile Connection connection;

    public RabbitMQ() {
        connectionFactory.setAutomaticRecoveryEnabled(true);
        user("rabbitmq");       // default user/password
        password("rabbitmq");
        pool = new Pool<>(this::createChannel, Channel::close);
        pool.name("rabbitmq");
        pool.size(1, 20);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(5));
    }

    public void close() {
        if (connection != null) {
            logger.info("close rabbitMQ client, hosts={}", Arrays.toString(addresses));
            try {
                pool.close();
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public void user(String user) {
        connectionFactory.setUsername(user);
    }

    public void password(String password) {
        connectionFactory.setPassword(password);
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
        pool.checkoutTimeout(timeout);
    }

    public void slowMessageThreshold(Duration slowMessageThreshold) {
        slowMessageThresholdInMs = slowMessageThreshold.toMillis();
    }

    public RabbitMQConsumer consumer(String queue, int prefetchCount) {
        Channel channel = createChannel();
        return new RabbitMQConsumer(channel, queue, prefetchCount);
    }

    public void publish(String exchange, String routingKey, String message, AMQP.BasicProperties properties) {
        StopWatch watch = new StopWatch();
        PoolItem<Channel> item = pool.borrowItem();
        try {
            item.resource.basicPublish(exchange, routingKey, properties, Strings.bytes(message));
        } catch (AlreadyClosedException e) {    // rabbitmq throws AlreadyClosedException for channel error, e.g. channel is not configured correctly or not exists
            item.broken = true;
            throw e;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("publish, exchange={}, routingKey={}, elapsedTime={}", exchange, routingKey, elapsedTime);
            checkSlowMessage(elapsedTime);
        }
    }

    private void checkSlowMessage(long elapsedTime) {
        if (elapsedTime > slowMessageThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_MESSAGE"), "slow rabbitmq message, elapsedTime={}", elapsedTime);
        }
    }

    public Channel createChannel() {
        try {
            if (connection == null) {
                createConnection();
            }
            return connection.createChannel();
        } catch (IOException | TimeoutException e) {
            throw new Error(e);
        }
    }

    private void createConnection() throws IOException, TimeoutException {
        lock.lock();
        try {
            if (connection == null)
                connection = connectionFactory.newConnection(addresses);
        } finally {
            lock.unlock();
        }
    }
}
