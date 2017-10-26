package core.framework.impl.redis;

import core.framework.redis.RedisHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisHashImplTest {
    private ByteArrayOutputStream request;
    private MockRedisFactory.ResponseHolder response;
    private RedisHash redis;

    @BeforeEach
    void createRedisHash() {
        request = new ByteArrayOutputStream();
        response = new MockRedisFactory.ResponseHolder();
        redis = MockRedisFactory.create(request, response).hash();
    }

    @Test
    void get() {
        response.data = "$2\r\nv1\r\n";
        String value = redis.get("key", "f1");

        assertEquals("v1", value);
        assertEquals("*3\r\n$4\r\nHGET\r\n$3\r\nkey\r\n$2\r\nf1\r\n", decode(request.toByteArray()));
    }

    @Test
    void set() {
        response.data = ":1\r\n";
        redis.set("key", "f1", "v1");

        assertEquals("*4\r\n$4\r\nHSET\r\n$3\r\nkey\r\n$2\r\nf1\r\n$2\r\nv1\r\n", decode(request.toByteArray()));
    }

    @Test
    void getAll() {
        response.data = "*4\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n$1\r\n4\r\n";
        Map<String, String> values = redis.getAll("key");

        assertEquals(2, values.size());
        assertEquals("2", values.get("1"));
        assertEquals("4", values.get("3"));
        assertEquals("*2\r\n$7\r\nHGETALL\r\n$3\r\nkey\r\n", decode(request.toByteArray()));
    }
}
