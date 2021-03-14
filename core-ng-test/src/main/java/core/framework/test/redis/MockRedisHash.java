package core.framework.test.redis;

import core.framework.redis.RedisHash;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
public final class MockRedisHash implements RedisHash {
    private final MockRedisStore store;

    MockRedisHash(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public Map<String, String> getAll(String key) {
        var value = store.get(key);
        if (value == null) return Map.of();
        return Map.copyOf(value.hash());
    }

    @Override
    public String get(String key, String field) {
        var value = store.get(key);
        if (value == null) return null;
        return value.hash().get(field);
    }

    @Override
    public void set(String key, String field, String value) {
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();
        hash.put(field, value);
    }

    @Override
    public void multiSet(String key, Map<String, String> values) {
        assertThat(values).isNotEmpty();
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();
        hash.putAll(values);
    }

    @Override
    public long increaseBy(String key, String field, long increment) {
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();

        String value = hash.get(field);
        if (value == null) {
            hash.put(field, String.valueOf(increment));
            return increment;
        }
        long result = Long.parseLong(value) + increment;
        hash.put(field, String.valueOf(result));
        return result;
    }

    @Override
    public long del(String key, String... fields) {
        assertThat(fields).isNotEmpty();
        MockRedisStore.Value value = store.get(key);
        if (value == null) return 0;
        long deleted = 0;
        Map<String, String> hash = value.hash();
        for (String field : fields) {
            String previous = hash.remove(field);
            if (previous != null) deleted++;
        }
        return deleted;
    }
}
