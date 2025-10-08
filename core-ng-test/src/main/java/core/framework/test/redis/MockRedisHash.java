package core.framework.test.redis;

import core.framework.redis.RedisHash;
import core.framework.util.Maps;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
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

        Map<String, MockRedisStore.HashValue> hash = value.hash();
        Map<String, String> results = Maps.newHashMapWithExpectedSize(hash.size());
        long now = System.currentTimeMillis();
        for (Map.Entry<String, MockRedisStore.HashValue> entry : hash.entrySet()) {
            MockRedisStore.HashValue hashValue = entry.getValue();
            if (!hashValue.expired(now)) {
                results.put(entry.getKey(), hashValue.value);
            }
        }
        return results;
    }

    @Override
    @Nullable
    public String get(String key, String field) {
        var value = store.get(key);
        if (value == null) return null;

        return get(value.hash(), field);
    }

    @Nullable
    private String get(Map<String, MockRedisStore.HashValue> hash, String field) {
        MockRedisStore.HashValue hashValue = hash.get(field);
        if (hashValue == null) return null;
        long now = System.currentTimeMillis();
        if (hashValue.expired(now)) {
            return null;
        } else {
            return hashValue.value;
        }
    }

    @Override
    public void set(String key, String field, String value) {
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();
        hash.put(field, new MockRedisStore.HashValue(value));
    }

    @Override
    public void multiSet(String key, Map<String, String> values) {
        assertThat(values).isNotEmpty();
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();

        for (var entry : values.entrySet()) {
            String value = entry.getValue();
            hash.put(entry.getKey(), new MockRedisStore.HashValue(value));
        }
    }

    @Override
    public long increaseBy(String key, String field, long increment) {
        var hash = store.putIfAbsent(key, new HashMap<>()).hash();

        String value = get(hash, field);
        if (value == null) {
            hash.put(field, new MockRedisStore.HashValue(String.valueOf(increment)));
            return increment;
        }
        long result = Long.parseLong(value) + increment;
        hash.put(field, new MockRedisStore.HashValue(String.valueOf(result)));
        return result;
    }

    @Override
    public long del(String key, String... fields) {
        assertThat(fields).isNotEmpty();
        MockRedisStore.Value value = store.get(key);
        if (value == null) return 0;
        long deleted = 0;
        Map<String, MockRedisStore.HashValue> hash = value.hash();
        for (String field : fields) {
            var previous = hash.remove(field);
            if (previous != null) deleted++;
        }
        return deleted;
    }

    @Override
    public void expire(String key, String field, Duration duration) {
        var value = store.get(key);
        if (value == null) return;
        final MockRedisStore.HashValue hashValue = value.hash().get(field);
        if (hashValue != null) {
            long now = System.currentTimeMillis();
            hashValue.expirationTime = now + duration.toMillis();
        }
    }
}
