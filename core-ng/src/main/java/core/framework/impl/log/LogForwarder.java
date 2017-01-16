package core.framework.impl.log;

import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.api.util.Threads;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.PerformanceStatMessage;
import core.framework.impl.log.queue.StatMessage;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class LogForwarder {
    private static final int MAX_TRACE_LENGTH = 1000000; // 1M

    private final Logger logger = LoggerFactory.getLogger(LogForwarder.class);
    private final String appName;

    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private final KafkaProducer<String, byte[]> kafkaProducer;

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread logForwarderThread;
    private final JSONWriter<ActionLogMessage> actionLogWriter = JSONWriter.of(ActionLogMessage.class);
    private final JSONWriter<StatMessage> statWriter = JSONWriter.of(StatMessage.class);
    private final Callback callback = (metadata, exception) -> {
        if (exception != null) {
            logger.warn("failed to send log message", exception);
            queue.clear();
        }
    };

    public LogForwarder(String uri, String appName) {
        this.appName = appName;
        Map<String, Object> config = Maps.newHashMap();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ProducerConfig.ACKS_CONFIG, "0");    // no acknowledge to maximize performance
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
        kafkaProducer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started, uri={}", uri);
            while (!stop.get()) {
                try {
                    Object message = queue.take();
                    if (message instanceof ActionLogMessage) {
                        kafkaProducer.send(new ProducerRecord<>("action-log", actionLogWriter.toJSON((ActionLogMessage) message)), callback);
                    } else if (message instanceof StatMessage) {
                        kafkaProducer.send(new ProducerRecord<>("stat", statWriter.toJSON((StatMessage) message)), callback);
                    }
                } catch (Throwable e) {
                    if (!stop.get()) {
                        logger.warn("failed to send log message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        });
        logForwarderThread.setName("log-forwarder");
        logForwarderThread.setPriority(Thread.NORM_PRIORITY - 1);
    }

    public void start() {
        logForwarderThread.start();
    }

    public void stop() {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        kafkaProducer.close(5, TimeUnit.SECONDS);
    }

    void forwardLog(ActionLog log) {
        ActionLogMessage message = new ActionLogMessage();
        message.app = appName;
        message.serverIP = Network.localHostAddress();
        message.id = log.id;
        message.date = log.date;
        message.result = log.result();
        message.refId = log.refId;
        message.elapsed = log.elapsed;
        message.cpuTime = log.cpuTime;
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
                String traceMessage = event.logMessage();
                if (builder.length() + traceMessage.length() >= MAX_TRACE_LENGTH) {
                    builder.append(traceMessage.substring(0, MAX_TRACE_LENGTH - builder.length()));
                    builder.append("...(truncated)");
                    break;
                }
                builder.append(traceMessage);
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
