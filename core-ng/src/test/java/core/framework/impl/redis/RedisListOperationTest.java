package core.framework.impl.redis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisListOperationTest extends AbstractRedisOperationTest {
    @Test
    void push() {
        response(":2\r\n");
        long length = redis.list().push("key", "item1", "item2");

        assertThat(length).isEqualTo(2);
        assertRequestEquals("*4\r\n$5\r\nRPUSH\r\n$3\r\nkey\r\n$5\r\nitem1\r\n$5\r\nitem2\r\n");
    }

    @Test
    void pop() {
        response("$5\r\nitem1\r\n");
        String item = redis.list().pop("key");

        assertThat(item).isEqualTo("item1");
        assertRequestEquals("*2\r\n$4\r\nLPOP\r\n$3\r\nkey\r\n");
    }

    @Test
    void range() {
        response("*2\r\n$5\r\nitem1\r\n$5\r\nitem2\r\n");
        List<String> items = redis.list().range("key");

        assertThat(items).containsExactly("item1", "item2");
        assertRequestEquals("*4\r\n$6\r\nLRANGE\r\n$3\r\nkey\r\n$1\r\n0\r\n$2\r\n-1\r\n");
    }
}
