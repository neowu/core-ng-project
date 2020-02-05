package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * @author neo
 */
public class MonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(MonitorJob.class);
    private final Collector collector;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;

    public MonitorJob(Collector collector, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.collector = collector;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        var message = new StatMessage();
        message.id = UUID.randomUUID().toString();
        message.date = Instant.now();
        message.app = app;
        message.host = host;
        try {
            Stats stats = collector.collect();
            message.result = stats.result();
            message.stats = stats.stats;
            message.errorCode = stats.errorCode;
            message.errorMessage = stats.errorMessage;
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            message.result = "ERROR";
            message.errorCode = "FAILED_TO_COLLECT";
            message.errorMessage = e.getMessage();
        }
        publisher.publish(message);
    }
}
