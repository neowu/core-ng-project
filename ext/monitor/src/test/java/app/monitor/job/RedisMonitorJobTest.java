package app.monitor.job;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisMonitorJobTest {
    private RedisMonitorJob job;

    @BeforeEach
    void createRedisMonitorJob() {
        job = new RedisMonitorJob(null, "app", "host", null);
        job.highMemUsageThreshold = 0.8;
    }

    @Test
    void stats() {
        Stats stats = job.stats(Map.of("maxmemory", "0",
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
        assertThat(job.keys(Map.of())).isEqualTo(0);
    }
}
