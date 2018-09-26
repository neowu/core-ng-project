package core.framework.impl.log.filter;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MapLogParamTest {
    @Test
    void append() {
        var param = new MapLogParam(Map.of("SessionId", "123", "key1", "value1"));
        var builder = new StringBuilder();
        param.append(builder, Set.of("SessionId"), 1000);
        assertThat(builder.toString()).contains("SessionId=******").contains("key1=value1");
    }

    @Test
    void appendWithTruncation() {
        Map<String, String> values = new LinkedHashMap<>(); // make map order deterministic for result
        values.put("k1", "v1");
        values.put("k2", "v2");

        var param = new MapLogParam(values);
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 7);
        assertThat(builder.toString())
                .isEqualTo("{k1=v1,...(truncated)");
    }
}
