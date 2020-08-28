package core.framework.internal.log.filter;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.bytes;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesMapLogParamTest {
    @Test
    void append() {
        var param = new BytesMapLogParam(Map.of("k1", bytes("v1"), "k2", bytes("v2")));
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString())
                .contains("k1=v1")
                .contains("k2=v2");
    }

    @Test
    void appendWithTruncation() {
        Map<String, byte[]> values = new LinkedHashMap<>(); // make map order deterministic for result
        values.put("k1", bytes("v1"));
        values.put("k2", bytes("v2"));

        var param = new BytesMapLogParam(values);
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 5);
        assertThat(builder.toString())
                .isEqualTo("{k1=v...(truncated)");
    }
}
