package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
public class MockRedisSortedSetTest {
    private RedisSortedSet sortedSet;

    @BeforeEach
    void createMockRedisSortedSet() {
        sortedSet = new MockRedis().sortedSet();
    }

    @Test
    void add() {
        assertThat(sortedSet.add("key", "1", 1, true)).isTrue();
        assertThat(sortedSet.add("key", "1", 2, false)).isTrue();
    }

    @Test
    void addOnlyIfAbsent() {
        assertThat(sortedSet.add("key", "1", 1, true)).isTrue();
        assertThat(sortedSet.add("key", "1", 2, true)).isFalse();
    }

    @Test
    void multiAdd() {
        Map<String, Long> values = Map.ofEntries(entry("1", 1L), entry("2", 2L));
        assertThat(sortedSet.multiAdd("key", values, false)).isEqualTo(2);
        assertThat(sortedSet.multiAdd("key", values, false)).isEqualTo(2);
    }

    @Test
    void multiAddOnlyIfAbsent() {
        Map<String, Long> values = Map.ofEntries(entry("1", 1L), entry("2", 2L));
        assertThat(sortedSet.multiAdd("key", values, true)).isEqualTo(2);
        Map<String, Long> anotherValues = Map.ofEntries(entry("1", 1L), entry("3", 3L));
        assertThat(sortedSet.multiAdd("key", anotherValues, true)).isEqualTo(1);
    }

    @Test
    void popByScore() {
        sortedSet.add("key", "1", 1, false);
        sortedSet.add("key", "2", 2, false);
        sortedSet.add("key", "3", 3, false);
        sortedSet.add("key", "4", 4, false);

        assertThat(sortedSet.popByScore("key", -10, 0)).isEmpty();
        assertThat(sortedSet.popByScore("key", 0, 1, 1)).containsExactly(entry("1", 1L));
        assertThat(sortedSet.popByScore("key", 0, 4, 2)).containsExactly(entry("2", 2L), entry("3", 3L));
    }

    @Test
    void rangeByScore() {
        sortedSet.add("key", "1", 1, false);
        sortedSet.add("key", "2", 2, false);
        sortedSet.add("key", "3", 3, false);

        assertThat(sortedSet.rangeByScore("key", -10, 0)).isEmpty();

        assertThat(sortedSet.rangeByScore("key", 0, 4, 1))
            .containsExactly(entry("1", 1L));
        assertThat(sortedSet.rangeByScore("key", 0, 4, 1).keySet().toArray())
            .isEqualTo(new String[]{"1"});

        assertThat(sortedSet.rangeByScore("key", 0, 4, 3))
            .containsExactly(entry("1", 1L), entry("2", 2L), entry("3", 3L));
        assertThat(sortedSet.rangeByScore("key", 0, 4, 3).keySet().toArray())
            .isEqualTo(new String[]{"1", "2", "3"});

        assertThat(sortedSet.rangeByScore("key", 0, 4, 4))
            .containsExactly(entry("1", 1L), entry("2", 2L), entry("3", 3L));
        assertThat(sortedSet.rangeByScore("key", 0, 4, 4).keySet().toArray())
            .isEqualTo(new String[]{"1", "2", "3"});
    }

    @Test
    void range() {
        sortedSet.add("key", "4", 4, false);
        sortedSet.add("key", "1", 1, false);
        sortedSet.add("key", "2", 2, false);
        sortedSet.add("key", "3", 3, false);

        assertThat(sortedSet.range("key", 0, -1))
            .containsExactly(entry("1", 1L), entry("2", 2L), entry("3", 3L), entry("4", 4L));

        assertThat(sortedSet.range("key", 2, -1))
            .containsExactly(entry("3", 3L), entry("4", 4L));

        assertThat(sortedSet.range("key", 1, 2))
            .containsExactly(entry("2", 2L), entry("3", 3L));
        assertThat(sortedSet.range("key", 1, 2).keySet().toArray())
            .isEqualTo(new String[]{"2", "3"});

        assertThat(sortedSet.range("key", -1, 5))
            .containsOnlyKeys(List.of("1", "2", "3", "4"));
        assertThat(sortedSet.range("key", 9, 10)).isEmpty();
    }
}
