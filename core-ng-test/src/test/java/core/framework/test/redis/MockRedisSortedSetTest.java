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
class MockRedisSortedSetTest {
    private RedisSortedSet sortedSet;

    @BeforeEach
    void createMockRedisSortedSet() {
        sortedSet = new MockRedis().sortedSet();
    }

    @Test
    void add() {
        assertThat(sortedSet.add("key", "1", 1)).isTrue();
        assertThat(sortedSet.add("key", "1", 2)).isTrue();

        Map<String, Long> values = Map.of("1", 1L, "2", 2L);
        assertThat(sortedSet.add("key", values, false)).isEqualTo(2);
        assertThat(sortedSet.add("key", values, false)).isEqualTo(2);
    }

    @Test
    void increaseScoreBy() {
        assertThat(sortedSet.increaseScoreBy("key", "1", 1)).isEqualTo(1);
        assertThat(sortedSet.increaseScoreBy("key", "1", 1)).isEqualTo(2);
        assertThat(sortedSet.increaseScoreBy("key", "2", 2)).isEqualTo(2);
        assertThat(sortedSet.increaseScoreBy("key", "2", 2)).isEqualTo(4);

        assertThat(sortedSet.range("key", 0, -1)).containsExactly(entry("1", 2L), entry("2", 4L));
    }

    @Test
    void addOnlyIfAbsent() {
        assertThat(sortedSet.add("key1", Map.of("1", 1L), true)).isEqualTo(1);
        assertThat(sortedSet.add("key1", Map.of("1", 2L), true)).isEqualTo(0);

        assertThat(sortedSet.add("key2", Map.of("1", 1L, "2", 2L), true)).isEqualTo(2);
        assertThat(sortedSet.add("key2", Map.of("1", 1L, "3", 3L), true)).isEqualTo(1);
    }

    @Test
    void popByScore() {
        sortedSet.add("key", "1", 1);
        sortedSet.add("key", "2", 2);
        sortedSet.add("key", "3", 3);
        sortedSet.add("key", "4", 4);

        assertThat(sortedSet.popByScore("key", -10, 0)).isEmpty();
        assertThat(sortedSet.popByScore("key", 0, 1, 1)).containsExactly(entry("1", 1L));
        assertThat(sortedSet.popByScore("key", 0, 4, 2)).containsExactly(entry("2", 2L), entry("3", 3L));
    }

    @Test
    void rangeByScore() {
        sortedSet.add("key", "1", 1);
        sortedSet.add("key", "2", 2);
        sortedSet.add("key", "3", 3);

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
        sortedSet.add("key", "4", 4);
        sortedSet.add("key", "1", 1);
        sortedSet.add("key", "2", 2);
        sortedSet.add("key", "3", 3);

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

    @Test
    void popMin() {
        sortedSet.add("key", "1", 1);
        sortedSet.add("key", "3", 3);
        sortedSet.add("key", "2", 2);
        sortedSet.add("key", "4", 4);

        assertThat(sortedSet.popMin("key")).isEqualTo("1");
        assertThat(sortedSet.popMin("key", 2)).containsExactly(entry("2", 2L), entry("3", 3L));
        assertThat(sortedSet.popMin("key", 5)).containsExactly(entry("4", 4L));
    }

    @Test
    void remove() {
        sortedSet.add("key", "1", 1);
        sortedSet.add("key", "3", 3);
        sortedSet.add("key", "2", 2);
        sortedSet.add("key", "4", 4);

        assertThat(sortedSet.remove("key", "2", "3")).isEqualTo(2);
        assertThat(sortedSet.remove("key", "4", "3")).isEqualTo(1);
        assertThat(sortedSet.popMin("key")).isEqualTo("1");
        assertThat(sortedSet.popMin("key")).isNull();
    }
}
