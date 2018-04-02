package core.framework.impl.log.param;

import core.framework.util.Maps;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MapParamTest {
    @Test
    void encode() {
        Map<String, byte[]> values = Maps.newHashMap();
        values.put("key1", Strings.bytes("value1"));
        values.put("key2", Strings.bytes("value2"));
        assertEquals("{key1=value1, key2=value2}", String.valueOf(new MapParam(values)));
    }
}
