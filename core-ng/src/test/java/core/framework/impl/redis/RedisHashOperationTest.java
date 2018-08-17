package core.framework.impl.redis;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class RedisHashOperationTest extends AbstractRedisOperationTest {
    @Test
    void get() {
        response("$2\r\nv1\r\n");
        String value = redis.hash().get("key", "f1");

        assertThat(value).isEqualTo("v1");
        assertRequestEquals("*3\r\n$4\r\nHGET\r\n$3\r\nkey\r\n$2\r\nf1\r\n");
    }

    @Test
    void set() {
        response(":1\r\n");
        redis.hash().set("key", "f1", "v1");

        assertRequestEquals("*4\r\n$4\r\nHSET\r\n$3\r\nkey\r\n$2\r\nf1\r\n$2\r\nv1\r\n");
    }

    @Test
    void getAll() {
        response("*4\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n$1\r\n4\r\n");
        Map<String, String> values = redis.hash().getAll("key");

        assertThat(values).containsExactly(entry("1", "2"), entry("3", "4"));
        assertRequestEquals("*2\r\n$7\r\nHGETALL\r\n$3\r\nkey\r\n");
    }

    @Test
    void multiSet() {
        response("+OK\r\n");
        redis.hash().multiSet("key", Map.of("f2", "v2", "f1", "v1"));

        assertRequestEquals("*6\r\n$5\r\nHMSET\r\n$3\r\nkey\r\n$2\r\nf1\r\n$2\r\nv1\r\n$2\r\nf2\r\n$2\r\nv2\r\n");
    }

    @Test
    void del() {
        response(":1\r\n");
        long deleted = redis.hash().del("key", "f1");

        assertThat(deleted).isEqualTo(1);
        assertRequestEquals("*3\r\n$4\r\nHDEL\r\n$3\r\nkey\r\n$2\r\nf1\r\n");
    }
}
