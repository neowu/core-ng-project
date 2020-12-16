package core.framework.internal.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class RedisPubSubOperationTest extends AbstractRedisOperationTest {
    @Test
    void publish() {
        response(":1\r\n");
        redis.pubSub().publish("channel", Strings.bytes("message"));

        assertRequestEquals("*3\r\n$7\r\nPUBLISH\r\n$7\r\nchannel\r\n$7\r\nmessage\r\n");
    }
}
