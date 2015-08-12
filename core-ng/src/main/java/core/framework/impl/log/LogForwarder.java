package core.framework.impl.log;

import com.rabbitmq.client.AMQP;
import core.framework.api.queue.Message;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author neo
 */
public class LogForwarder {
    private final Logger logger = LoggerFactory.getLogger(LogForwarder.class);
    private final PrintStream fallbackLogger = System.err;
    private final String appName;
    private final String actionLogMessageType;

    private final BlockingQueue<ActionLogMessage> actionLogQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final RabbitMQ rabbitMQ = new RabbitMQ();

    public LogForwarder(String host, String appName) {
        rabbitMQ.hosts(host);
        this.appName = appName;
        actionLogMessageType = ActionLogMessage.class.getAnnotation(Message.class).name();
    }

    public void initialize() {
        executor.submit(() -> {
            Thread thread = Thread.currentThread();
            thread.setName("log-forwarder-action-log");
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            while (!executor.isShutdown()) {
                try {
                    sendActionLogs();
                } catch (Exception e) {
                    logger.warn("failed to send action log, retry in 30 seconds");
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
            }
        });
    }

    private void sendActionLogs() throws InterruptedException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().type(actionLogMessageType).build();

        try (RabbitMQChannel channel = rabbitMQ.channel()) {
            while (!executor.isShutdown()) {
                ActionLogMessage message = actionLogQueue.take();
                channel.publish("", "action-log-queue", JSON.toJSON(message), properties);
            }
        }
    }

    public void shutdown() {
        logger.info("shutdown log forwarder");
        executor.shutdown();
        rabbitMQ.shutdown();
    }

    public void sendActionLog(ActionLog log) {
        ActionLogMessage message = new ActionLogMessage();
        message.app = appName;
        message.id = log.id;
        message.date = log.startTime;
        message.result = log.result();
        message.refId = log.refId;
        message.elapsed = log.elapsed;
        message.action = log.action;
        message.errorMessage = log.errorMessage;
        if (log.exceptionClass != null)
            message.exceptionClass = log.exceptionClass.getCanonicalName();
        message.context = log.context;

        Map<String, PerformanceStatMessage> performanceStats = Maps.newLinkedHashMap();
        log.performanceStats.forEach((key, stat) -> {
            PerformanceStatMessage statMessage = new PerformanceStatMessage();
            statMessage.count = stat.count;
            statMessage.totalElapsed = stat.totalElapsed;
            performanceStats.put(key, statMessage);
        });
        message.performanceStats = performanceStats;

        try {
            actionLogQueue.put(message);
        } catch (InterruptedException e) {
            fallbackLogger.println(Strings.format("failed to queue action log message", e));
        }
    }
}
