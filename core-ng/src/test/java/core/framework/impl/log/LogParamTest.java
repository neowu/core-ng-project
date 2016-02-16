package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class LogParamTest {
    @Test
    public void encodeMap() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("key1", Strings.bytes("value1"));
        values.put("key2", Strings.bytes("value2"));
        assertEquals("{key1=value1, key2=value2}", String.valueOf(LogParam.of(values)));
    }

    @Test
    public void truncateLongString() {
        String message = "1234567890";
        LogParam.StringParam param = new LogParam.StringParam(Strings.bytes(message), Charsets.UTF_8, 5);
        assertEquals("1234...(truncated)", param.toString());
    }
}