package core.framework.impl.redis;

import core.framework.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class RedisSetOperationTest extends AbstractRedisOperationTest {
    @Test
    void add() {
        response(":1\r\n");
        boolean added = redis.set().add("key", "item1");

        assertTrue(added);
        assertRequestEquals("*3\r\n$4\r\nSADD\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }

    @Test
    void members() {
        response("*3\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n");
        Set<String> members = redis.set().members("key");

        assertEquals(Sets.newHashSet("1", "2", "3"), members);
        assertRequestEquals("*2\r\n$8\r\nSMEMBERS\r\n$3\r\nkey\r\n");
    }

    @Test
    void isMember() {
        response(":1\r\n");
        boolean isMember = redis.set().isMember("key", "item1");

        assertTrue(isMember);
        assertRequestEquals("*3\r\n$9\r\nSISMEMBER\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }

    @Test
    void remove() {
        response(":1\r\n");
        boolean removed = redis.set().remove("key", "item1");

        assertTrue(removed);
        assertRequestEquals("*3\r\n$4\r\nSREM\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }
}
