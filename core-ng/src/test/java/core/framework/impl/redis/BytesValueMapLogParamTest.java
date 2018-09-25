package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesValueMapLogParamTest {
    @Test
    void append() {
        var param = new BytesValueMapLogParam(Map.of("k1", Strings.bytes("v1"), "k2", Strings.bytes("v2")));
        var builder = new StringBuilder();
        param.append(builder, Set.of());
        assertThat(builder.toString())
                .contains("k1=v1")
                .contains("k2=v2");
    }
}
