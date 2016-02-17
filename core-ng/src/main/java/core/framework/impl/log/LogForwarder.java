package core.framework.impl.log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.PerformanceStatMessage;
import core.framework.impl.log.queue.StatMessage;
import core.framework.impl.queue.RabbitMQImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class LogForwarder {
    private final Logger logger = LoggerFactory.getLogger(LogForwarder.class);
    private final String appName;

    private final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();

    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private final RabbitMQImpl rabbitMQ = new RabbitMQImpl();

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread logForwarderThread;
    private final JSONWriter<ActionLogMessage> actionLogWriter = JSONWriter.of(ActionLogMessage.class);
    private final JSONWriter<StatMessage> statWriter = JSONWriter.of(StatMessage.class);
    private int retryAttempts;

    public LogForwarder(String host, String appName) {
        this.appName = appName;
        rabbitMQ.hosts(host);

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started");
            while (!stop.get()) {
                try {
                    forwardLogs();
                } catch (Throwable e) {
                    if (!stop.get()) {  // if not initiated by shutdown, exception types can be ShutdownSignalException, InterruptedException
                        retryAttempts++;
                        if (retryAttempts >= 10) {  // roughly 5 mins to hold messages if log queue is not available
                            queue.clear();    // clear messages to clean up memory
                        }
                        logger.warn("failed to send log message, retry in 30 seconds, attempts={}", retryAttempts, e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        });
        logForwarderThread.setName("log-forwarder");
        logForwarderThread.setPriority(Thread.NORM_PRIORITY - 1);
    }

    void start() {
        logForwarderThread.start();
    }

    void stop() {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        rabbitMQ.close();
    }

    private void forwardLogs() throws InterruptedException, IOException {
        Channel channel = rabbitMQ.createChannel();
        try {
            while (!stop.get()) {
                Object message = queue.take();

                if (message instanceof ActionLogMessage) {
                    channel.basicPublish("", "action-log-queue", properties, actionLogWriter.toJSON((ActionLogMessage) message));
                } else if (message instanceof StatMessage) {
                    channel.basicPublish("", "stat-queue", properties, statWriter.toJSON((StatMessage) message));
                }

                retryAttempts = 0;  // reset retry attempts if one message sent successfully
            }
        } finally {
            closeChannel(channel);
        }
    }

    private void closeChannel(Channel channel) {
        try {
            channel.close();
        } catch (ShutdownSignalException e) {
            logger.debug("connection is closed", e);
        } catch (IOException | TimeoutException e) {
            logger.warn("failed to close channel", e);
        }
    }

    void forwardLog(ActionLog log) {
        ActionLogMessage message = new ActionLogMessage();
        message.app = appName;
        message.serverIP = Network.localHostAddress();
        message.id = log.id;
        message.date = log.startTime;
        message.result = log.result();
        message.refId = log.refId;
        message.elapsed = log.elapsed;
        message.action = log.action;
        message.errorCode = log.errorCode();
        message.errorMessage = log.errorMessage;
        message.context = log.context;
        Map<String, PerformanceStatMessage> performanceStats = Maps.newLinkedHashMap();
        log.performanceStats.forEach((key, stat) -> {
            PerformanceStatMessage statMessage = new PerformanceStatMessage();
            statMessage.count = stat.count;
            statMessage.totalElapsed = stat.totalElapsed;
            performanceStats.put(key, statMessage);
        });
        message.performanceStats = performanceStats;
        if (log.flushTraceLog()) {
            StringBuilder builder = new StringBuilder(log.events.size() << 8);  // length * 256 as rough initial capacity
            for (LogEvent event : log.events) {
                builder.append(event.logMessage());
            }
            message.traceLog = builder.toString();
        }
        queue.add(message);
    }

    public void forwardStats(Map<String, Double> stats) {
        StatMessage message = new StatMessage();
        message.id = UUID.randomUUID().toString();
        message.date = Instant.now();
        message.app = appName;
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        queue.add(message);
    }
}
