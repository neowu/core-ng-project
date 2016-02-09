package core.log.queue;

import core.framework.api.util.JSON;
import core.framework.api.util.Threads;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.StatMessage;
import core.framework.impl.queue.RabbitMQ;
import core.log.service.ActionManager;
import core.log.service.StatManager;
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
public class LogProcessor {
    private final Logger logger = LoggerFactory.getLogger(LogProcessor.class);

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread actionThread;
    private final Thread statThread;
    private final RabbitMQ rabbitMQ;
    private final ActionManager actionManager;
    private final StatManager statManager;

    public LogProcessor(String queueHost, ActionManager actionManager, StatManager statManager) {
        rabbitMQ = new RabbitMQ();
        rabbitMQ.hosts(queueHost);
        this.actionManager = actionManager;
        this.statManager = statManager;
        actionThread = new Thread(() -> {
            logger.info("action processor thread started");
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = new RabbitMQConsumer(rabbitMQ.createChannel(), "action-log-queue", 4000)) {    // prefetch twice as batch size
                    processAction(consumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
            logger.info("action processor thread stopped");
        });
        statThread = new Thread(() -> {
            logger.info("stat processor thread started");
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = new RabbitMQConsumer(rabbitMQ.createChannel(), "stat-queue", 4000)) {    // prefetch twice as batch size
                    processStats(consumer);
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        logger.error("failed to process message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
            logger.info("stat processor thread stopped");
        });
    }

    private void processAction(RabbitMQConsumer consumer) throws InterruptedException, IOException {
        List<ActionLogMessage> messages = new LinkedList<>();
        long lastDeliveryTag = 0;

        while (!stop.get()) {
            RabbitMQConsumer.Message message = consumer.poll();
            if (message != null) {
                ActionLogMessage log = JSON.fromJSON(ActionLogMessage.class, message.body);
                messages.add(log);
                lastDeliveryTag = message.deliveryTag;
            }
            if (messages.size() >= 2000 || (message == null && !messages.isEmpty())) {
                actionManager.index(messages);
                messages = new LinkedList<>();
                consumer.acknowledgeAll(lastDeliveryTag);
            }
            if (message == null) {
                Thread.sleep(5000);
            }
        }
    }

    private void processStats(RabbitMQConsumer consumer) throws InterruptedException, IOException {
        List<StatMessage> messages = new LinkedList<>();
        long lastDeliveryTag = 0;

        while (!stop.get()) {
            RabbitMQConsumer.Message message = consumer.poll();
            if (message != null) {
                StatMessage log = JSON.fromJSON(StatMessage.class, message.body);
                messages.add(log);
                lastDeliveryTag = message.deliveryTag;
            }
            if (messages.size() >= 2000 || (message == null && !messages.isEmpty())) {
                statManager.index(messages);
                messages = new LinkedList<>();
                consumer.acknowledgeAll(lastDeliveryTag);
            }
            if (message == null) {
                Thread.sleep(5000);
            }
        }
    }

    public void start() {
        actionThread.start();
        statThread.start();
    }

    public void stop() {
        logger.info("stop log processor");
        stop.set(true);
        actionThread.interrupt();
        statThread.interrupt();
        rabbitMQ.close();
    }
}
