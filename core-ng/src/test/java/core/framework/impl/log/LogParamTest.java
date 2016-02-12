package core.framework.impl.log;

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
}