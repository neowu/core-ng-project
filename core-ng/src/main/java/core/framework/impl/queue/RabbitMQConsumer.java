package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.utility.Utility;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * @author neo
 */
public class RabbitMQConsumer implements Consumer, AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final QueueingConsumer.Delivery stopSignal = new QueueingConsumer.Delivery(null, null, null);
    private final Queue<QueueingConsumer.Delivery> queue = new ConcurrentLinkedQueue<>();
    private final Channel channel;
    private final Thread consumerThread;
    private volatile ShutdownSignalException shutdown;
    private volatile ConsumerCancelledException cancelled;

    // refer to com.rabbitmq.client.QueueingConsumer
    public RabbitMQConsumer(Channel channel, String queue, int prefetchCount) {
        this.channel = channel;
        try {
            channel.basicQos(prefetchCount);
            channel.basicConsume(queue, false, this);   // QOS only works with manual ack
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        consumerThread = Thread.currentThread();
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException shutdown) {
        this.shutdown = shutdown;
        queue.add(stopSignal);
        LockSupport.unpark(consumerThread);
    }

    @Override
    public void handleRecoverOk(String consumerTag) {

    }

    @Override
    public void handleConsumeOk(String consumerTag) {

    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        cancelled = new ConsumerCancelledException();
        queue.add(stopSignal);
        LockSupport.unpark(consumerThread);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        if (shutdown != null) throw Utility.fixStackTrace(shutdown);
        queue.add(new QueueingConsumer.Delivery(envelope, properties, body));
        LockSupport.unpark(consumerThread);
    }

    public QueueingConsumer.Delivery nextDelivery() throws ShutdownSignalException, ConsumerCancelledException, InterruptedException {
        while (true) {
            QueueingConsumer.Delivery delivery = poll();
            if (delivery != null) {
                return delivery;
            } else {
                park();
            }
        }
    }

    public Deque<QueueingConsumer.Delivery> nextDeliveries(int maxSize) throws ShutdownSignalException, ConsumerCancelledException, InterruptedException {
        while (true) {
            QueueingConsumer.Delivery delivery = poll();
            if (delivery != null) {
                Deque<QueueingConsumer.Delivery> deliveries = new LinkedList<>();
                deliveries.add(delivery);
                while (deliveries.size() < maxSize) {
                    delivery = poll();
                    if (delivery == null) break;
                    deliveries.add(delivery);
                }
                return deliveries;
            } else {
                park();
            }
        }
    }

    private QueueingConsumer.Delivery poll() {
        QueueingConsumer.Delivery delivery = queue.poll();
        if (stopSignal.equals(delivery) || delivery == null && (shutdown != null || cancelled != null)) {
            if (stopSignal.equals(delivery)) queue.add(stopSignal);
            if (shutdown != null) throw Utility.fixStackTrace(shutdown);
            if (cancelled != null) throw Utility.fixStackTrace(cancelled);
        }
        return delivery;
    }

    private void park() throws InterruptedException {
        LockSupport.park();
        if (Thread.interrupted()) throw new InterruptedException();
    }

    public void acknowledgeAll(long deliveryTag) {
        acknowledge(deliveryTag, true);
    }

    public void acknowledge(long deliveryTag) {
        acknowledge(deliveryTag, false);
    }

    private void acknowledge(long deliveryTag, boolean multiple) {
        StopWatch watch = new StopWatch();
        try {
            channel.basicAck(deliveryTag, multiple);
        } catch (IOException | AlreadyClosedException e) {
            logger.error("failed to acknowledge message due to network issue, rabbitMQ will resend message if connection is closed", e);
            throw new Error(e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitMQ", elapsedTime);
            logger.debug("acknowledge, queue={}, deliveryTag={}, multiple={}, elapsedTime={}", queue, deliveryTag, multiple, elapsedTime);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            channel.close();
        } catch (ShutdownSignalException e) {
            logger.debug("connection is closed", e);
        } catch (IOException | TimeoutException e) {
            logger.warn("failed to close channel", e);
        }
    }
}
