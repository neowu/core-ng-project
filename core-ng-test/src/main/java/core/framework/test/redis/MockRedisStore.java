package core.framework.test.redis;

import core.framework.util.Maps;

import java.io.Serial;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MockRedisStore {
    final Map<String, Value> store = Maps.newConcurrentHashMap();

    Value get(String key) {
        Value value = store.get(key);
        if (value == null) return null;
        if (value.expired(System.currentTimeMillis())) {
            store.remove(key);
            return null;
        }
        return value;
    }

    Value putIfAbsent(String key, Object value) {
        return store.computeIfAbsent(key, k -> new Value(value));
    }

    static final class Value {
        final Object value;
        Long expirationTime;

        Value(Object value) {
            this.value = value;
        }

        String string() {
            assertThat(value).isInstanceOf(String.class);
            return (String) value;
        }

        @SuppressWarnings("unchecked")
        List<String> list() {
            assertThat(value).isInstanceOf(List.class);
            return (List<String>) value;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> hash() {
            assertThat(value).isInstanceOf(Map.class);
            return (Map<String, String>) value;
        }

        @SuppressWarnings("unchecked")
        Set<String> set() {
            assertThat(value).isInstanceOf(Set.class);
            return (Set<String>) value;
        }

        @SuppressWarnings("PMD.LooseCoupling")
            // intentional design
        SortedSet sortedSet() {
            assertThat(value).isInstanceOf(SortedSet.class);
            return (SortedSet) value;
        }

        @SuppressWarnings("PMD.LooseCoupling")
            // intentional design
        HyperLogLog hyperLogLog() {
            assertThat(value).isInstanceOf(HyperLogLog.class);
            return (HyperLogLog) value;
        }

        boolean expired(long now) {
            return expirationTime != null && now >= expirationTime;
        }
    }

    // to distinguish with hash key type
    static class SortedSet extends HashMap<String, Long> {  // Map<value, score>
        @Serial
        private static final long serialVersionUID = -1982891940392623040L;
    }

    // use hashset as a naive impl, may be replaced by bloom filter in future
    static class HyperLogLog extends HashSet<String> {
        @Serial
        private static final long serialVersionUID = -4584074052672348286L;
    }
}
