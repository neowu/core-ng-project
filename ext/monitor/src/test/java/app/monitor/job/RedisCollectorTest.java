package app.monitor.job;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisCollectorTest {
    private RedisCollector collector;

    @BeforeEach
    void createRedisCollector() {
        collector = new RedisCollector(null);
        collector.highMemUsageThreshold = 0.8;
    }

    @Test
    void collect() {
        Stats stats = collector.collect(Map.of("maxmemory", "0",
                "total_system_memory", "1000000",
                "used_memory", "800000",
                "db0", "keys=5,expires=0,avg_ttl=0"));

        assertThat(stats.result()).isEqualTo("WARN");
        assertThat(stats.errorCode).isEqualTo("HIGH_MEM_USAGE");
        assertThat(stats.stats)
                .containsEntry("redis_keys", 5d)
                .containsEntry("redis_mem_used", 800000d);
    }

    @Test
    void keys() {
        assertThat(collector.keys(Map.of())).isEqualTo(0);
    }
}
