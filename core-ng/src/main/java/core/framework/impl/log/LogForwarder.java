package core.framework.impl.log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import core.framework.api.queue.Message;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.ActionLogMessages;
import core.framework.impl.log.queue.PerformanceStatMessage;
import core.framework.impl.queue.RabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class LogForwarder {
    private final Logger logger = LoggerFactory.getLogger(LogForwarder.class);
    private final String appName;

    private final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().type(ActionLogMessages.class.getAnnotation(Message.class).name()).build();

    private final Queue<ActionLogMessage> queue = new ConcurrentLinkedQueue<>();
    private final RabbitMQ rabbitMQ = new RabbitMQ();

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread logForwarderThread;
    private int retryAttempts;

    public LogForwarder(String host, String appName) {
        this.appName = appName;
        rabbitMQ.hosts(host);

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started");
            while (!stop.get()) {
                try {
                    sendActionLogMessages();
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

    private void sendActionLogMessages() throws InterruptedException, IOException {
        Channel channel = rabbitMQ.createChannel();
        try {
            List<ActionLogMessage> logs = new LinkedList<>();
            int messageSize = 0;
            while (!stop.get()) {
                ActionLogMessage message = queue.poll();
                if (message != null) {
                    logs.add(message);
                    messageSize += 1000;    // action log without trace is roughly 1k
                    messageSize += message.traceLog == null ? 0 : message.traceLog.length();
                }

                if ((message == null && !logs.isEmpty()) || logs.size() >= 2000 || messageSize >= 5000000) {    // send if more than 2000 logs or larger than 5M
                    ActionLogMessages messages = new ActionLogMessages();
                    messages.logs = logs;
                    channel.basicPublish("", "action-log-queue", properties, Strings.bytes(JSON.toJSON(messages)));
                    retryAttempts = 0;  // reset retry attempts if one message sent successfully
                    logs = new LinkedList<>();
                    messageSize = 0;
                }

                if (message == null) {
                    Thread.sleep(5000);
                }
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

    void forward(ActionLog log) {
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
            StringBuilder traceLog = new StringBuilder(log.events.size() * 64);
            for (LogEvent event : log.events) {
                traceLog.append(event.logMessage());
            }
            message.traceLog = traceLog.toString();
        }

        queue.add(message);
    }
}
