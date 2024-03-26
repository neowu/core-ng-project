package core.framework.test.redis;

import core.framework.redis.RedisHyperLogLog;
import core.framework.test.redis.MockRedisStore.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
public class MockRedisHyperLogLog implements RedisHyperLogLog {
    private final MockRedisStore store;

    MockRedisHyperLogLog(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public boolean add(String key, String... values) {
        assertThat(values).isNotEmpty().doesNotContainNull();
        var log = store.putIfAbsent(key, new MockRedisStore.HyperLogLog()).hyperLogLog();
        return Collections.addAll(log, values);
    }

    @Override
    public long count(String... keys) {
        Set<String> merged = new HashSet<>();
        for (String key : keys) {
            Value value = store.get(key);
            if (value != null) {
                merged.addAll(value.hyperLogLog());
            }
        }
        return merged.size();
    }
}
