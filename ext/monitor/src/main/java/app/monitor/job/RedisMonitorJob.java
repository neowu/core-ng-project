package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.redis.Redis;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            Map<String, String> info = redis.admin().info();
            Stats stats = stats(info);
            publisher.publish(StatMessageFactory.stats(app, host, stats));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            publisher.publish(StatMessageFactory.failedToCollect(app, host, e));
        }
    }

    Stats stats(Map<String, String> info) {
        var stats = new Stats();
        double maxMem = get(info, "maxmemory");
        if (maxMem == 0) maxMem = get(info, "total_system_memory");
        stats.put("redis_mem_max", maxMem);
        double usedMem = get(info, "used_memory");
        stats.put("redis_mem_used", usedMem);
        stats.checkHighUsage(usedMem / maxMem, highMemUsageThreshold, "mem");

        long keys = keys(info);
        stats.put("redis_keys", keys);

        return stats;
    }

    long keys(Map<String, String> info) {
        String keySpace = info.get("db0");      // e.g. keys=5,expires=0,avg_ttl=0
        if (keySpace == null) return 0;         // db0 returns null if there is not any key
        int index = keySpace.indexOf(',');
        return Long.parseLong(keySpace.substring(5, index));
    }

    private double get(Map<String, String> info, String key) {
        String value = info.get(key);
        if (value == null) throw new Error("can not find key from info, key=" + key);
        return Double.parseDouble(value);
    }
}
