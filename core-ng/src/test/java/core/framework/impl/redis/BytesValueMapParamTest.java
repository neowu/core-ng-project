package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesValueMapParamTest {
    @Test
    void convertToString() {
        var values = Map.of("k1", Strings.bytes("v1"), "k2", Strings.bytes("v2"));
        assertThat(new BytesValueMapParam(values).toString())
                .startsWith("{").endsWith("}")
                .contains("k1=v1").contains("k2=v2");
    }
}
