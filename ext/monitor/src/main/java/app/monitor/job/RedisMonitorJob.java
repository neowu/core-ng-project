package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.redis.Redis;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class RedisMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(RedisMonitorJob.class);
    private final Redis redis;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;
    public double highMemUsageThreshold;

    public RedisMonitorJob(Redis redis, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.redis = redis;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.app = app;
        message.host = host;
        try {
            Map<String, String> info = redis.admin().info();
            Stats stats = stats(info);
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

    Stats stats(Map<String, String> info) {
        var stats = new Stats();
        double maxMem = get(info, "maxmemory");
        if (maxMem == 0) maxMem = get(info, "total_system_memory");
        stats.put("redis_mem_max", maxMem);
        double usedMem = get(info, "used_memory");
        stats.put("redis_mem_used", usedMem);
        stats.checkHighUsage(usedMem / maxMem, highMemUsageThreshold, "mem");

        int keys = keys(info);
        stats.put("redis_keys", keys);

        return stats;
    }

    int keys(Map<String, String> info) {
        String keySpace = info.get("db0");      // e.g. keys=5,expires=0,avg_ttl=0
        if (keySpace == null) return 0;         // db0 returns null if there is not any key
        int index = keySpace.indexOf(',');
        return Integer.parseInt(keySpace.substring(5, index));
    }

    private double get(Map<String, String> info, String key) {
        String value = info.get(key);
        if (value == null) throw new Error("can not find key from info, key=" + key);
        return Double.parseDouble(value);
    }
}
