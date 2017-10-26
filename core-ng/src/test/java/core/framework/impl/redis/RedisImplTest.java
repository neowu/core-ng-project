package core.framework.impl.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisImplTest {
    private ByteArrayOutputStream requestStream;

    @BeforeEach
    void createRequestStream() {
        requestStream = new ByteArrayOutputStream();
    }

    @Test
    void get() {
        RedisImpl redis = MockRedisFactory.create(requestStream, "$6\r\nfoobar\r\n");
        String value = redis.get("key");

        assertEquals("foobar", value);
        assertEquals("*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n", requestStream.toString());
    }

    @Test
    void set() {
        RedisImpl redis = MockRedisFactory.create(requestStream, "+OK\r\n");
        redis.set("key", "value");

        assertEquals("*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n", requestStream.toString());
    }
}
