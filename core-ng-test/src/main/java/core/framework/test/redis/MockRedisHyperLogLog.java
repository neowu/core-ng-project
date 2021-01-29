package core.framework.test.redis;

import core.framework.redis.RedisHyperLogLog;
import core.framework.test.redis.MockRedisStore.Value;
import core.framework.util.Sets;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
public class MockRedisHyperLogLog implements RedisHyperLogLog {
    private final MockRedisStore store;

    public MockRedisHyperLogLog(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public boolean add(String key, String... values) {
        assertThat(values).isNotEmpty().doesNotContainNull();
        Value value = store.putIfAbsent(key, Sets.newConcurrentHashSet());
        Set<String> valueSet = value.set();
        return Collections.addAll(valueSet, values);
    }

    @Override
    public long count(String... keys) {
        long count = 0;
        for (String key : keys) {
            Value value = store.get(key);
            if (value != null) {
                count += value.set().size();
            }
        }
        return count;
    }

    @Override
    public long merge(String destinationKey, String... sourceKeys) {
        Value value = store.putIfAbsent(destinationKey, Sets.newConcurrentHashSet());
        Set<String> destinationSet = value.set();
        for (String sourceKey : sourceKeys) {
            Value sourceValue = store.get(sourceKey);
            if (sourceValue != null) {
                destinationSet.addAll(sourceValue.set());
            }
        }
        return destinationSet.size();
    }
}
