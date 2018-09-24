package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesValueMapLogParamTest {
    @Test
    void convertToString() {
        var values = Map.of("k1", Strings.bytes("v1"), "k2", Strings.bytes("v2"));
        assertThat(new BytesValueMapLogParam(values).toString())
                .contains("k1=v1").contains("k2=v2");
    }
}
