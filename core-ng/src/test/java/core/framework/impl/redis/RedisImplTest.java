package core.framework.impl.redis;

import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Map;

import static core.framework.impl.redis.RedisEncodings.decode;
import static core.framework.impl.redis.RedisEncodings.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        response.data = "$6\r\nfoobar\r\n";
        String value = redis.get("key");

        assertEquals("foobar", value);
        assertEquals("*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n", decode(request.toByteArray()));
    }

    @Test
    void set() {
        response.data = "+OK\r\n";
        redis.set("key", "value");

        assertEquals("*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n", decode(request.toByteArray()));
    }

    @Test
    void setWithExpiration() {
        response.data = "+OK\r\n";
        redis.set("key", "value", Duration.ofMinutes(1));

        assertEquals("*4\r\n$5\r\nSETEX\r\n$3\r\nkey\r\n$2\r\n60\r\n$5\r\nvalue\r\n", decode(request.toByteArray()));
    }

    @Test
    void setIfAbsent() {
        response.data = "+OK\r\n";
        boolean result = redis.setIfAbsent("key", "value", Duration.ofMinutes(1));

        assertTrue(result);
        assertEquals("*6\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nnx\r\n$2\r\nex\r\n$2\r\n60\r\n", decode(request.toByteArray()));
    }

    @Test
    void expire() {
        response.data = ":1\r\n";
        redis.expire("key", Duration.ofMinutes(1));

        assertEquals("*3\r\n$6\r\nEXPIRE\r\n$3\r\nkey\r\n$2\r\n60\r\n", decode(request.toByteArray()));
    }

    @Test
    void multiSet() {
        response.data = "+OK\r\n";
        Map<String, String> values = Maps.newLinkedHashMap();
        values.put("k1", "v1");
        values.put("k2", "v2");
        redis.multiSet(values);

        assertEquals("*5\r\n$4\r\nMSET\r\n$2\r\nk1\r\n$2\r\nv1\r\n$2\r\nk2\r\n$2\r\nv2\r\n", decode(request.toByteArray()));
    }

    @Test
    void multiSetWithExpiration() {
        response.data = "+OK\r\n+OK\r\n";
        Map<String, byte[]> values = Maps.newLinkedHashMap();
        values.put("k1", encode("v1"));
        values.put("k2", encode("v2"));
        redis.multiSet(values, Duration.ofMinutes(1));

        assertEquals("*4\r\n$5\r\nSETEX\r\n$2\r\nk1\r\n$2\r\n60\r\n$2\r\nv1\r\n"
                + "*4\r\n$5\r\nSETEX\r\n$2\r\nk2\r\n$2\r\n60\r\n$2\r\nv2\r\n", decode(request.toByteArray()));
    }

    @Test
    void close() {
        redis.close();
    }
}
