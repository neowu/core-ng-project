package core.framework.internal.redis;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class RedisSortedSetOperationTest extends AbstractRedisOperationTest {
    @Test
    void add() {
        response(":1\r\n");
        boolean added = redis.sortedSet().add("key", "v1", 100);

        assertThat(added).isTrue();
        assertRequestEquals("*4\r\n$4\r\nZADD\r\n$3\r\nkey\r\n$3\r\n100\r\n$2\r\nv1\r\n");
    }

    @Test
    void addOnlyIfAbsent() {
        response(":0\r\n");
        int added = redis.sortedSet().add("key", Map.of("v1", 100L), true);

        assertThat(added).isEqualTo(0);
        assertRequestEquals("*5\r\n$4\r\nZADD\r\n$3\r\nkey\r\n$2\r\nNX\r\n$3\r\n100\r\n$2\r\nv1\r\n");
    }

    @Test
    void increaseScoreBy() {
        response("$1\r\n3\r\n");
        long score = redis.sortedSet().increaseScoreBy("key", "value", 2);

        assertThat(score).isEqualTo(3);
        assertRequestEquals("*4\r\n$7\r\nZINCRBY\r\n$3\r\nkey\r\n$1\r\n2\r\n$5\r\nvalue\r\n");
    }

    @Test
    void range() {
        response("*4\r\n$2\r\nv1\r\n$3\r\n100\r\n$2\r\nv2\r\n$3\r\n200\r\n");
        Map<String, Long> values = redis.sortedSet().range("key");

        assertThat(values).containsExactly(entry("v1", 100L), entry("v2", 200L));
        assertRequestEquals("*5\r\n$6\r\nZRANGE\r\n$3\r\nkey\r\n$1\r\n0\r\n$2\r\n-1\r\n$10\r\nWITHSCORES\r\n");
    }

    @Test
    void rangeByScore() {
        response("*4\r\n$2\r\nv1\r\n$3\r\n100\r\n$2\r\nv2\r\n$3\r\n200\r\n");
        Map<String, Long> values = redis.sortedSet().rangeByScore("key", 100, 200);

        assertThat(values).containsExactly(entry("v1", 100L), entry("v2", 200L));
        assertRequestEquals("*9\r\n$6\r\nZRANGE\r\n$3\r\nkey\r\n$3\r\n100\r\n$3\r\n200\r\n$7\r\nBYSCORE\r\n$10\r\nWITHSCORES\r\n$5\r\nLIMIT\r\n$1\r\n0\r\n$2\r\n-1\r\n");
    }

    @Test
    void popByScore() {
        response("*4\r\n$2\r\nv1\r\n$3\r\n100\r\n$2\r\nv2\r\n$3\r\n200\r\n"
                 + ":1\r\n"
                 + ":1\r\n");
        Map<String, Long> values = redis.sortedSet().popByScore("key", 100, 200);

        assertThat(values).containsExactly(entry("v1", 100L), entry("v2", 200L));
        assertRequestEquals("*9\r\n$6\r\nZRANGE\r\n$3\r\nkey\r\n$3\r\n100\r\n$3\r\n200\r\n$7\r\nBYSCORE\r\n$10\r\nWITHSCORES\r\n$5\r\nLIMIT\r\n$1\r\n0\r\n$2\r\n-1\r\n"
                            + "*3\r\n$4\r\nZREM\r\n$3\r\nkey\r\n$2\r\nv1\r\n"
                            + "*3\r\n$4\r\nZREM\r\n$3\r\nkey\r\n$2\r\nv2\r\n");
    }

    @Test
    void popByScoreWithLimit() {
        response("*4\r\n$2\r\nv1\r\n$3\r\n100\r\n$2\r\nv2\r\n$3\r\n200\r\n"
                 + ":1\r\n");
        Map<String, Long> values = redis.sortedSet().popByScore("key", 100, 200, 1);

        assertThat(values).containsExactly(entry("v1", 100L));
        assertRequestEquals("*9\r\n$6\r\nZRANGE\r\n$3\r\nkey\r\n$3\r\n100\r\n$3\r\n200\r\n$7\r\nBYSCORE\r\n$10\r\nWITHSCORES\r\n$5\r\nLIMIT\r\n$1\r\n0\r\n$2\r\n-1\r\n"
                            + "*3\r\n$4\r\nZREM\r\n$3\r\nkey\r\n$2\r\nv1\r\n");
    }

    @Test
    void popMin() {
        response("*2\r\n$3\r\none\r\n$1\r\n1\r\n");
        String value = redis.sortedSet().popMin("key");

        assertThat(value).isEqualTo("one");
        assertRequestEquals("*3\r\n$7\r\nZPOPMIN\r\n$3\r\nkey\r\n$1\r\n1\r\n");
    }

    @Test
    void remove() {
        response(":1\r\n");
        long removed = redis.sortedSet().remove("key", "item1");

        assertThat(removed).isEqualTo(1);
        assertRequestEquals("*3\r\n$4\r\nZREM\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }
}
