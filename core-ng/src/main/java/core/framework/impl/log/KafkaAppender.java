package core.framework.impl.log;

import core.framework.impl.json.JSONWriter;
import core.framework.impl.kafka.ProducerMetrics;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.LogTopics;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.impl.log.message.StatMessage;
import core.framework.util.Maps;
import core.framework.util.Network;
import core.framework.util.Threads;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
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
public final class KafkaAppender {
    private static final int MAX_TRACE_LENGTH = 1000000; // 1M

    public static KafkaAppender create(String uri, String appName) {
        Map<String, Object> config = Maps.newHashMap();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ProducerConfig.ACKS_CONFIG, "0");    // no acknowledge to maximize performance
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder");
        Producer<String, byte[]> producer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());
        return new KafkaAppender(uri, appName, producer);
    }

    public final ProducerMetrics producerMetrics;
    final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private final Logger logger = LoggerFactory.getLogger(KafkaAppender.class);
    private final String appName;
    private final Producer<String, byte[]> producer;

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

    KafkaAppender(String uri, String appName, Producer<String, byte[]> producer) {
        this.appName = appName;
        this.producer = producer;

        producerMetrics = new ProducerMetrics("log-forwarder");
        producerMetrics.set(producer.metrics());

        logForwarderThread = new Thread(() -> {
            logger.info("log forwarder thread started, uri={}", uri);
            while (!stop.get()) {
                try {
                    Object message = queue.take();
                    if (message instanceof ActionLogMessage) {
                        producer.send(new ProducerRecord<>(LogTopics.TOPIC_ACTION_LOG, actionLogWriter.toJSON((ActionLogMessage) message)), callback);
                    } else if (message instanceof StatMessage) {
                        producer.send(new ProducerRecord<>(LogTopics.TOPIC_STAT, statWriter.toJSON((StatMessage) message)), callback);
                    }
                } catch (Throwable e) {
                    if (!stop.get()) {
                        logger.warn("failed to send log message, retry in 30 seconds", e);
                        Threads.sleepRoughly(Duration.ofSeconds(30));
                    }
                }
            }
        }, "log-forwarder");
        logForwarderThread.setPriority(Thread.NORM_PRIORITY - 1);
    }

    public void start() {
        logForwarderThread.start();
    }

    public void stop() {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        producer.close(5, TimeUnit.SECONDS);
    }

    void forward(ActionLog log) {
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
        message.stats = log.stats;
        Map<String, PerformanceStatMessage> performanceStats = Maps.newLinkedHashMap();
        log.performanceStats.forEach((key, stat) -> {
            PerformanceStatMessage statMessage = new PerformanceStatMessage();
            statMessage.count = stat.count;
            statMessage.totalElapsed = stat.totalElapsed;
            statMessage.readEntries = stat.readEntries;
            statMessage.writeEntries = stat.writeEntries;
            performanceStats.put(key, statMessage);
        });
        message.performanceStats = performanceStats;
        if (log.flushTraceLog()) {
            StringBuilder builder = new StringBuilder(log.events.size() << 8);  // length * 256 as rough initial capacity
            for (LogEvent event : log.events) {
                String traceMessage = event.logMessage();
                if (builder.length() + traceMessage.length() >= MAX_TRACE_LENGTH) {
                    builder.append(traceMessage, 0, MAX_TRACE_LENGTH - builder.length());
                    builder.append("...(truncated)");
                    break;
                }
                builder.append(traceMessage);
            }
            message.traceLog = builder.toString();
        }
        queue.add(message);
    }

    public void forward(Map<String, Double> stats) {
        StatMessage message = new StatMessage();
        message.id = UUID.randomUUID().toString();
        message.date = Instant.now();
        message.app = appName;
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        queue.add(message);
    }
}
