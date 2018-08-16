package core.framework.impl.log;

import core.framework.impl.json.JSONWriter;
import core.framework.impl.kafka.ProducerMetrics;
import core.framework.impl.log.filter.LogFilter;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.LogTopics;
import core.framework.impl.log.message.StatMessage;
import core.framework.util.Maps;
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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class KafkaAppender implements Appender {
    public final ProducerMetrics producerMetrics;
    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
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

    public KafkaAppender(String uri, String appName) {
        this.appName = appName;

        Map<String, Object> config = Maps.newHashMap();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uri);
        config.put(ProducerConfig.ACKS_CONFIG, "0");    // no acknowledge to maximize performance
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Duration.ofSeconds(30).toMillis());  // metadata update timeout
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "log-forwarder");
        producer = new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());

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

    public void stop(long timeoutInMs) {
        logger.info("stop log forwarder");
        stop.set(true);
        logForwarderThread.interrupt();
        producer.close(timeoutInMs <= 0 ? 1000 : timeoutInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void append(ActionLog log, LogFilter filter) {
        queue.add(MessageFactory.actionLog(log, appName, filter));
    }

    public void forward(Map<String, Double> stats) {
        queue.add(MessageFactory.stat(stats, appName));
    }
}
