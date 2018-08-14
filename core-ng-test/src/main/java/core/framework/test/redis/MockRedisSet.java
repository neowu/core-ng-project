package core.framework.test.redis;

import core.framework.redis.RedisSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
public final class MockRedisSet implements RedisSet {
    private final MockRedisStore store;

    MockRedisSet(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public boolean add(String key, String... values) {
        assertThat(values).doesNotContainNull();
        var setValue = store.putIfAbsent(key, new HashSet<>());
        return Collections.addAll(setValue.set(), values);
    }

    @Override
    public Set<String> members(String key) {
        var value = store.get(key);
        if (value == null) return Set.of();
        return Set.copyOf(value.set());
    }

    @Override
    public boolean isMember(String key, String value) {
        var redisValue = store.get(key);
        if (redisValue == null) return false;
        return redisValue.set().contains(value);
    }

    @Override
    public boolean remove(String key, String... values) {
        var redisValue = store.get(key);
        if (redisValue == null) return false;
        return redisValue.set().removeAll(Arrays.asList(values));
    }
}
