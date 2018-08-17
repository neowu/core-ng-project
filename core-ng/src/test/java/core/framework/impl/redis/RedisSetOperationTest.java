package core.framework.impl.redis;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisSetOperationTest extends AbstractRedisOperationTest {
    @Test
    void add() {
        response(":1\r\n");
        long added = redis.set().add("key", "item1");

        assertThat(added).isEqualTo(1);
        assertRequestEquals("*3\r\n$4\r\nSADD\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }

    @Test
    void members() {
        response("*3\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n");
        Set<String> members = redis.set().members("key");

        assertThat(members).containsOnly("1", "2", "3");
        assertRequestEquals("*2\r\n$8\r\nSMEMBERS\r\n$3\r\nkey\r\n");
    }

    @Test
    void isMember() {
        response(":1\r\n");
        boolean isMember = redis.set().isMember("key", "item1");

        assertThat(isMember).isTrue();
        assertRequestEquals("*3\r\n$9\r\nSISMEMBER\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }

    @Test
    void remove() {
        response(":1\r\n");
        long removed = redis.set().remove("key", "item1");

        assertThat(removed).isEqualTo(1);
        assertRequestEquals("*3\r\n$4\r\nSREM\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }
}
