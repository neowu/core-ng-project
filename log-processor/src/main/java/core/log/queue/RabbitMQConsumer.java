package core.log.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

/**
 * @author neo
 */
public class RabbitMQConsumer implements Consumer, AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final RabbitMQMessage stopSignal = new RabbitMQMessage(0, null);
    private final Queue<RabbitMQMessage> queue = new ConcurrentLinkedQueue<>();
    private final Channel channel;
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
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException shutdown) {
        this.shutdown = shutdown;
        queue.add(stopSignal);
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
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (shutdown != null) throw Utility.fixStackTrace(shutdown);
        long deliveryTag = envelope.getDeliveryTag();
        queue.add(new RabbitMQMessage(deliveryTag, body));
    }

    public RabbitMQMessage poll() throws ShutdownSignalException, ConsumerCancelledException {
        RabbitMQMessage message = queue.poll();
        if (stopSignal.equals(message) || message == null && (shutdown != null || cancelled != null)) {
            if (stopSignal.equals(message)) queue.add(stopSignal);
            if (shutdown != null) throw Utility.fixStackTrace(shutdown);
            if (cancelled != null) throw Utility.fixStackTrace(cancelled);
        }
        return message;
    }

    public void acknowledgeAll(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, true);
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
