package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.redis.Redis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class RedisMonitorJobTest {
    @Mock
    Redis redis;
    @Mock
    MessagePublisher<StatMessage> publisher;
    private RedisMonitorJob job;

    @BeforeEach
    void createRedisMonitorJob() {
        job = new RedisMonitorJob(redis, "redis", "localhost", publisher);
        job.highMemUsageThreshold = 0.8;
    }

    @Test
    void stats() {
        Stats stats = job.stats(Map.of("maxmemory", "0",
                "total_system_memory", "1000000",
                "used_memory", "800001",
                "db0", "keys=5,expires=0,avg_ttl=0"));

        assertThat(stats.result()).isEqualTo("WARN");
        assertThat(stats.errorCode).isEqualTo("HIGH_MEM_USAGE");
        assertThat(stats.stats)
                .containsEntry("redis_keys", 5d)
                .containsEntry("redis_mem_used", 800001d);
    }

    @Test
    void keys() {
        assertThat(job.keys(Map.of())).isEqualTo(0);
    }

    @Test
    void publishError() {
        when(redis.admin()).thenThrow(new Error("mock"));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "redis".equals(message.app)
                && "ERROR".equals(message.result)
                && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
