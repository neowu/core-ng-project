package core.log.queue;

import core.framework.api.util.JSON;
import core.framework.api.util.Threads;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.queue.RabbitMQ;
import core.log.service.ActionLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class ActionLogProcessor {
    private final Logger logger = LoggerFactory.getLogger(ActionLogProcessor.class);

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread fetchThread;
    private final RabbitMQ rabbitMQ;
    private final ActionLogManager actionLogManager;
    private List<ActionLogMessage> logs = new LinkedList<>();
    private long lastDeliveryTag;

    public ActionLogProcessor(String queueHost, ActionLogManager actionLogManager) {
        rabbitMQ = new RabbitMQ();
        rabbitMQ.hosts(queueHost);
        this.actionLogManager = actionLogManager;
        fetchThread = new Thread(() -> {
            logger.info("log processor thread started");
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = new RabbitMQConsumer(rabbitMQ.createChannel(), "action-log-queue", 4000)) {    // prefetch twice as batch size
                    process(consumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        });
    }

    private void process(RabbitMQConsumer consumer) throws InterruptedException, IOException {
        while (!stop.get()) {
            RabbitMQConsumer.Message message = consumer.poll();
            if (message != null) {
                if ("action_log".equals(message.type)) {
                    ActionLogMessage log = JSON.fromJSON(ActionLogMessage.class, message.body);
                    logs.add(log);
                }
                lastDeliveryTag = message.deliveryTag;
            }
            if (logs.size() >= 2000 || (message == null && !logs.isEmpty())) {
                actionLogManager.index(logs);
                logs = new LinkedList<>();
                consumer.acknowledgeAll(lastDeliveryTag);
            }
            if (message == null) {
                Thread.sleep(5000);
            }
        }
    }

    public void start() {
        fetchThread.start();
    }

    public void stop() {
        logger.info("stop log processor thread");
        stop.set(true);
        fetchThread.interrupt();
        rabbitMQ.close();
    }
}
