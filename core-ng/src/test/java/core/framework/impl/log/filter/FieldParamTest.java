package core.framework.impl.log.filter;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FieldParamTest {
    @Test
    void filter() {
        var param = new FieldParam("SessionId", "value");
        String message = param.filter(Set.of("SessionId"));
        assertThat(message).isNotEqualTo("value");
    }

    @Test
    void formatArrayValue() {
        var param = new FieldParam("key", new Object[]{1, 2, 3});
        String message = param.filter(Set.of());
        assertThat(message).isEqualTo("[1, 2, 3]");
    }

    @Test
    void formatMapValue() {
        var param = new FieldParam("key", Map.of("k1", "v1"));
        String message = param.filter(Set.of());
        assertThat(message).contains("k1=v1").contains("{k1=v1}");
    }
}
