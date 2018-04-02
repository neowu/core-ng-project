package core.framework.impl.log.param;

import core.framework.util.Charsets;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class LogParamHelperTest {
    @Test
    void truncateLongString() {
        String message = "1234567890";
        String value = LogParamHelper.toString(Strings.bytes(message), Charsets.UTF_8, 5);
        assertEquals("1234...(truncated)", value);
    }
}
