package core.framework.test.redis;

import core.framework.redis.RedisSet;

import java.util.HashSet;
import java.util.Iterator;
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
    public long add(String key, String... values) {
        assertThat(values).isNotEmpty().doesNotContainNull();
        var setValue = store.putIfAbsent(key, new HashSet<>());
        Set<String> set = setValue.set();
        long addedValues = 0;
        for (String value : values) {
            if (set.add(value)) addedValues++;
        }
        return addedValues;
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
    public long remove(String key, String... values) {
        assertThat(values).isNotEmpty().doesNotContainNull();
        var redisValue = store.get(key);
        if (redisValue == null) return 0;
        Set<String> set = redisValue.set();
        long removedValues = 0;
        for (String value : values) {
            if (set.remove(value)) removedValues++;
        }
        return removedValues;
    }

    @Override
    public Set<String> pop(String key, long count) {
        var redisValue = store.get(key);
        if (redisValue == null) return Set.of();
        Set<String> set = redisValue.set();

        Set<String> results = new HashSet<>();
        long removed = 0;
        for (Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
            if (removed == count) break;

            String item = iterator.next();
            iterator.remove();
            results.add(item);
            removed++;
        }

        return Set.copyOf(results);
    }

    @Override
    public long size(String key) {
        var redisValue = store.get(key);
        if (redisValue == null) return 0;
        return redisValue.set().size();
    }
}
