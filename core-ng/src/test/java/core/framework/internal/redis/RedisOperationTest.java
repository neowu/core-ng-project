package core.framework.internal.redis;

import core.framework.util.Lists;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static core.framework.internal.redis.RedisEncodings.encode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class RedisOperationTest extends AbstractRedisOperationTest {
    @Test
    void get() {
        response("$6\r\nfoobar\r\n");
        String value = redis.get("key");

        assertThat(value).isEqualTo("foobar");
        assertRequestEquals("*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n");
    }

    @Test
    void set() {
        response("+OK\r\n");
        redis.set("key", "value");

        assertRequestEquals("*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n");
    }

    @Test
    void setWithExpiration() {
        response("+OK\r\n");
        redis.set("key", "value", Duration.ofMinutes(1));

        assertRequestEquals("*5\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nPX\r\n$5\r\n60000\r\n");
    }

    @Test
    void setIfAbsent() {
        response("+OK\r\n");
        boolean result = redis.set("key", "value", Duration.ofMinutes(1), true);

        assertThat(result).isTrue();
        assertRequestEquals("*6\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nNX\r\n$2\r\nPX\r\n$5\r\n60000\r\n");
    }

    @Test
    void setIfAbsentWithPresentKey() {
        response("$-1\r\n");
        boolean result = redis.set("key", "value", null, true);

        assertThat(result).isFalse();
        assertRequestEquals("*4\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nNX\r\n");
    }

    @Test
    void expire() {
        response(":1\r\n");
        redis.expire("key", Duration.ofMinutes(1));

        assertRequestEquals("*3\r\n$7\r\nPEXPIRE\r\n$3\r\nkey\r\n$5\r\n60000\r\n");
    }

    @Test
    void del() {
        response(":1\r\n");
        redis.del("key");

        assertRequestEquals("*2\r\n$3\r\nDEL\r\n$3\r\nkey\r\n");
    }

    @Test
    void multiGet() {
        response("*3\r\n$2\r\nv1\r\n$-1\r\n$2\r\nv3\r\n");
        Map<String, String> values = redis.multiGet("k1", "k2", "k3");

        assertThat(values).containsExactly(entry("k1", "v1"), entry("k3", "v3"));
        assertRequestEquals("*4\r\n$4\r\nMGET\r\n$2\r\nk1\r\n$2\r\nk2\r\n$2\r\nk3\r\n");
    }

    @Test
    void multiSet() {
        response("+OK\r\n");
        Map<String, String> values = new LinkedHashMap<>();
        values.put("k1", "v1");
        values.put("k2", "v2");
        redis.multiSet(values);

        assertRequestEquals("*5\r\n$4\r\nMSET\r\n$2\r\nk1\r\n$2\r\nv1\r\n$2\r\nk2\r\n$2\r\nv2\r\n");
    }

    @Test
    void multiSetWithExpiration() {
        response("+OK\r\n+OK\r\n");
        Map<String, byte[]> values = new LinkedHashMap<>();
        values.put("k1", encode("v1"));
        values.put("k2", encode("v2"));
        redis.multiSet(values, Duration.ofMinutes(1));

        assertRequestEquals("*5\r\n$3\r\nSET\r\n$2\r\nk1\r\n$2\r\nv1\r\n$2\r\nPX\r\n$5\r\n60000\r\n"
                + "*5\r\n$3\r\nSET\r\n$2\r\nk2\r\n$2\r\nv2\r\n$2\r\nPX\r\n$5\r\n60000\r\n");
    }

    @Test
    void increaseBy() {
        response(":1\r\n");
        redis.increaseBy("k1", 1);

        assertRequestEquals("*3\r\n$6\r\nINCRBY\r\n$2\r\nk1\r\n$1\r\n1\r\n");
    }

    @Test
    void forEach() {
        response("*2\r\n$1\r\n0\r\n*2\r\n$2\r\nk1\r\n$2\r\nk2\r\n");
        List<String> keys = Lists.newArrayList();
        redis.forEach("k*", keys::add);

        assertThat(keys).containsExactly("k1", "k2");
        assertRequestEquals("*6\r\n$4\r\nSCAN\r\n$1\r\n0\r\n$5\r\nMATCH\r\n$2\r\nk*\r\n$5\r\nCOUNT\r\n$3\r\n500\r\n");
    }

    @Test
    void expirationTime() {
        response(":1000\r\n:-1\r\n:-2\r\n");
        long[] expirationTimes = redis.expirationTime("k1", "k2", "k3");

        assertThat(expirationTimes).containsExactly(1000, -1, -2);
        assertRequestEquals("*2\r\n$4\r\nPTTL\r\n$2\r\nk1\r\n" + "*2\r\n$4\r\nPTTL\r\n$2\r\nk2\r\n" + "*2\r\n$4\r\nPTTL\r\n$2\r\nk3\r\n");
    }
}
