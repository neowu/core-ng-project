package core.framework.api.redis;

import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.impl.redis.RedisImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class RedisBenchmark {
    private RedisImpl redis;
    private Map<String, byte[]> values;

    @Setup
    public void setup() {
        redis = new RedisImpl();
        redis.host("52.6.213.238");

        values = Maps.newHashMap();
        values.put("key1", Strings.bytes("v1"));
        values.put("key2", Strings.bytes("v2"));
        values.put("key3", Strings.bytes("v3"));
        values.put("key4", Strings.bytes("v4"));
        values.put("key5", Strings.bytes("v5"));
    }

    @TearDown
    public void cleanup() {
        redis.close();
    }

    @Benchmark
    public void current() {
        redis.mset(values, Duration.ofMinutes(5));
    }
}
