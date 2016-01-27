package core.log.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.utility.Utility;
import core.framework.api.util.Charsets;
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
    // refer to com.rabbitmq.client.QueueingConsumer
    private static final Message STOP_SIGNAL = new Message(0, null, null);
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final Channel channel;
    private volatile ShutdownSignalException shutdown;
    private volatile ConsumerCancelledException cancelled;

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
        queue.add(STOP_SIGNAL);
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
        queue.add(STOP_SIGNAL);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (shutdown != null) throw Utility.fixStackTrace(shutdown);
        queue.add(new Message(envelope.getDeliveryTag(), properties.getType(), new String(body, Charsets.UTF_8)));
    }

    private Message handle(Message message) {
        if (STOP_SIGNAL.equals(message) || message == null && (shutdown != null || cancelled != null)) {
            if (STOP_SIGNAL.equals(message)) queue.add(STOP_SIGNAL);
            if (shutdown != null) throw Utility.fixStackTrace(shutdown);
            if (cancelled != null) throw Utility.fixStackTrace(cancelled);
        }
        return message;
    }

    public Message poll() throws ShutdownSignalException, ConsumerCancelledException {
        return handle(queue.poll());
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

    public static class Message {
        public final long deliveryTag;
        public final String type;
        public final String body;

        public Message(long deliveryTag, String type, String body) {
            this.deliveryTag = deliveryTag;
            this.type = type;
            this.body = body;
        }
    }
}
