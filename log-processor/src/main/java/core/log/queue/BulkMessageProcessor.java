package core.log.queue;

import com.rabbitmq.client.QueueingConsumer;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONReader;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class BulkMessageProcessor<T> {
    private final Logger logger = LoggerFactory.getLogger(BulkMessageProcessor.class);
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final String queue;
    private final int bulkSize;
    private final Consumer<List<T>> consumer;
    private final Thread processThread;
    private final JSONReader<T> reader;

    public BulkMessageProcessor(RabbitMQ rabbitMQ, String queue, Class<T> messageClass, int bulkSize, Consumer<List<T>> consumer) {
        this.queue = queue;
        this.bulkSize = bulkSize;
        this.consumer = consumer;
        reader = JSONReader.of(messageClass);
        processThread = new Thread(() -> {
            logger.info("message processor thread started, queue={}", queue);
            while (!stop.get()) {
                try (RabbitMQConsumer queueConsumer = rabbitMQ.consumer(queue, bulkSize * 2)) {
                    process(queueConsumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
            logger.info("message processor thread stopped, queue={}", queue);
        });
    }

    public void start() {
        processThread.start();
    }

    public void stop() {
        stop.set(true);
        processThread.interrupt();
    }

    private void process(RabbitMQConsumer consumer) throws IOException, InterruptedException {
        while (!stop.get()) {
            Deque<QueueingConsumer.Delivery> deliveries = consumer.nextDeliveries(bulkSize);
            try {
                int messageSize = 0;
                List<T> messages = new ArrayList<>(deliveries.size());
                for (QueueingConsumer.Delivery delivery : deliveries) {
                    byte[] body = delivery.getBody();
                    messages.add(reader.fromJSON(body));
                    messageSize += body.length;
                }
                consume(messages, messageSize);
                consumer.acknowledgeAll(deliveries.getLast().getEnvelope().getDeliveryTag());
            } catch (Throwable e) {
                // clear message on error, otherwise MQ will not send new messages due to prefetch value,
                // according to AMQP, acknowledge with deliveryTag=0 will acknowledge all outstanding messages
                consumer.acknowledgeAll(0);
                throw e;
            }
        }
    }

    private void consume(List<T> messages, int messageSize) {
        StopWatch watch = new StopWatch();
        try {
            consumer.accept(messages);
        } finally {
            long elapsedTime = watch.elapsedTime();
            logger.info("consume messages, queue={}, size={}, messageSize={}, elapsedTime={}", queue, messages.size(), messageSize, elapsedTime);
        }
    }
}
