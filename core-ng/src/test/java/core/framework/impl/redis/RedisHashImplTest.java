package core.framework.impl.redis;

import core.framework.redis.RedisHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisHashImplTest {
    private ByteArrayOutputStream requestStream;

    @BeforeEach
    void createRequestStream() {
        requestStream = new ByteArrayOutputStream();
    }

    @Test
    void set() {
        RedisHash redis = MockRedisFactory.create(requestStream, ":1\r\n").hash();
        redis.set("key", "f1", "v1");

        assertEquals("*3\r\n$4\r\nHSET\r\n$3\r\nkey\r\n$2\r\nv1\r\n", requestStream.toString());
    }

    @Test
    void getAll() {
        RedisHash redis = MockRedisFactory.create(requestStream, "*4\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n$1\r\n4\r\n").hash();
        Map<String, String> values = redis.getAll("key");

        assertEquals(2, values.size());
        assertEquals("2", values.get("1"));
        assertEquals("4", values.get("3"));
        assertEquals("*2\r\n$7\r\nHGETALL\r\n$3\r\nkey\r\n", requestStream.toString());
    }
}
