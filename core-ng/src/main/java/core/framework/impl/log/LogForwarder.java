package core.framework.impl.log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import core.framework.api.queue.Message;
import core.framework.api.util.JSON;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.PerformanceStatMessage;
import core.framework.impl.log.queue.TraceLogMessage;
import core.framework.impl.queue.RabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class LogForwarder {
    private final Logger logger = LoggerFactory.getLogger(LogForwarder.class);
    private final String appName;

    private final AMQP.BasicProperties actionLogMessageProperties = new AMQP.BasicProperties.Builder().type(ActionLogMessage.class.getAnnotation(Message.class).name()).build();
    private final AMQP.BasicProperties traceLogMessageProperties = new AMQP.BasicProperties.Builder().type(TraceLogMessage.class.getAnnotation(Message.class).name()).build();

    private final BlockingQueue<Object> logMessageQueue = new LinkedBlockingQueue<>();
    private final RabbitMQ rabbitMQ = new RabbitMQ();

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final Thread logForwarderThread;

    public LogForwarder(String host, String appName) {
        this.appName = appName;
        rabbitMQ.hosts(host);

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started");
            while (!shutdown.get()) {
                try {
                    sendLogMessages();
                } catch (InterruptedException e) {
                    // pass thru for interruption during shutdown
                } catch (Throwable e) {
                    logger.warn("failed to send log message, retry in 30 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
            }
        });
        logForwarderThread.setName("log-forwarder");
        logForwarderThread.setPriority(Thread.NORM_PRIORITY - 1);
    }

    public void start() {
        logForwarderThread.start();
    }

    private void sendLogMessages() throws InterruptedException, IOException {
        Channel channel = rabbitMQ.createChannel();
        try {
            while (!shutdown.get()) {
                Object message = logMessageQueue.take();
                if (message instanceof ActionLogMessage) {
                    channel.basicPublish("", "action-log-queue", actionLogMessageProperties, Strings.bytes(JSON.toJSON(message)));
                } else if (message instanceof TraceLogMessage) {
                    channel.basicPublish("", "trace-log-queue", traceLogMessageProperties, Strings.bytes(JSON.toJSON(message)));
                }
            }
        } finally {
            closeChannel(channel);
        }
    }

    private void closeChannel(Channel channel) {
        try {
            channel.close();
        } catch (IOException | TimeoutException e) {
            logger.warn("failed to close channel", e);
        }
    }

    public void shutdown() {
        logger.info("shutdown log forwarder");
        shutdown.set(true);
        logForwarderThread.interrupt();
        rabbitMQ.shutdown();
    }

    void queueActionLog(ActionLog log) {
        ActionLogMessage message = new ActionLogMessage();
        message.app = appName;
        message.serverIP = Network.localHostAddress();
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

        logMessageQueue.add(message);
    }

    void queueTraceLog(ActionLog log, List<LogEvent> events) {
        TraceLogMessage message = new TraceLogMessage();
        message.id = log.id;
        message.date = log.startTime;
        message.app = appName;
        message.action = log.action;
        message.result = log.result();

        StringBuilder content = new StringBuilder(events.size() * 64);
        for (LogEvent event : events) {
            content.append(event.logMessage());
        }
        message.content = Lists.newArrayList(content.toString());

        logMessageQueue.add(message);
    }
}
