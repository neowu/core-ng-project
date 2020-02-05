package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.redis.Redis;

import java.util.Map;

/**
 * @author neo
 */
public class RedisCollector implements Collector {
    private final Redis redis;
    private final double highMemUsageThreshold;

    public RedisCollector(Redis redis, double highMemUsageThreshold) {
        this.redis = redis;
        this.highMemUsageThreshold = highMemUsageThreshold;
    }

    @Override
    public Stats collect() {
        Map<String, String> info = redis.info();

        return collect(info);
    }

    Stats collect(Map<String, String> info) {
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
