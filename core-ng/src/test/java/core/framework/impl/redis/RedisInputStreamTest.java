package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisInputStreamTest {
    @Test
    void readSimpleString() throws IOException {
        RedisInputStream stream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes("line1\rline2\r\n")));
        String message = stream.readSimpleString();

        assertEquals("line1\rline2", message);
    }
}
