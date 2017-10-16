package core.framework.impl.log;

import core.framework.util.Charsets;
import core.framework.util.Maps;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class LogParamTest {
    @Test
    void encodeMap() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("key1", Strings.bytes("value1"));
        values.put("key2", Strings.bytes("value2"));
        assertEquals("{key1=value1, key2=value2}", String.valueOf(LogParam.of(values)));
    }

    @Test
    void truncateLongString() {
        String message = "1234567890";
        String value = LogParam.toString(Strings.bytes(message), Charsets.UTF_8, 5);
        assertEquals("1234...(truncated)", value);
    }
}
