package core.framework.internal.redis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tempo
 */
class RedisHyperLogLogOperationTest extends AbstractRedisOperationTest {
    @Test
    void add() {
        response(":1\r\n");
        boolean added = redis.hyperLogLog().add("key", "item1");

        assertThat(added).isTrue();
        assertRequestEquals("*3\r\n$5\r\nPFADD\r\n$3\r\nkey\r\n$5\r\nitem1\r\n");
    }

    @Test
    void count() {
        response(":1\r\n");
        assertThat(redis.hyperLogLog().count("key")).isEqualTo(1);
        assertRequestEquals("*2\r\n$7\r\nPFCOUNT\r\n$3\r\nkey\r\n");
    }
}
