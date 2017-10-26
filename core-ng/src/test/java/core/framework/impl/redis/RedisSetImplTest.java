package core.framework.impl.redis;

import core.framework.redis.RedisSet;
import core.framework.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class RedisSetImplTest {
    private ByteArrayOutputStream request;
    private MockRedisFactory.ResponseHolder response;
    private RedisSet redis;

    @BeforeEach
    void createRedisSet() {
        request = new ByteArrayOutputStream();
        response = new MockRedisFactory.ResponseHolder();
        redis = MockRedisFactory.create(request, response).set();
    }

    @Test
    void add() {
        response.response = ":1\r\n";

        boolean added = redis.add("key", "item1");

        assertTrue(added);
        assertEquals("*3\r\n$4\r\nSADD\r\n$3\r\nkey\r\n$5\r\nitem1\r\n", decode(request.toByteArray()));
    }

    @Test
    void members() {
        response.response = "*3\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n";
        Set<String> members = redis.members("key");

        assertEquals(Sets.newHashSet("1", "2", "3"), members);
        assertEquals("*2\r\n$8\r\nSMEMBERS\r\n$3\r\nkey\r\n", decode(request.toByteArray()));
    }
}
