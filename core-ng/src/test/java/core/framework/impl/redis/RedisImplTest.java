package core.framework.impl.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisImplTest {
    private ByteArrayOutputStream request;
    private MockRedisFactory.ResponseHolder response;
    private RedisImpl redis;

    @BeforeEach
    void createRedis() {
        request = new ByteArrayOutputStream();
        response = new MockRedisFactory.ResponseHolder();
        redis = MockRedisFactory.create(request, response);
    }

    @Test
    void get() {
        response.response = "$6\r\nfoobar\r\n";

        String value = redis.get("key");

        assertEquals("foobar", value);
        assertEquals("*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n", decode(request.toByteArray()));
    }

    @Test
    void set() {
        response.response = "+OK\r\n";

        redis.set("key", "value");

        assertEquals("*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n", decode(request.toByteArray()));
    }
}
