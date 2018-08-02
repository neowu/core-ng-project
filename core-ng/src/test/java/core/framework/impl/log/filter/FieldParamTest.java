package core.framework.impl.log.filter;

import core.framework.util.Sets;
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
        String message = param.filter(Set.of(new String[]{"SessionId"}));
        assertThat(message).isNotEqualTo("value");
    }

    @Test
    void formatArrayValue() {
        var param = new FieldParam("key", new Object[]{1, 2, 3});
        String message = param.filter(Sets.newHashSet());
        assertThat(message).isEqualTo("[1, 2, 3]");
    }

    @Test
    void formatMapValue() {
        var value = Map.of("k1", "v1", "k2", "v2");
        var param = new FieldParam("key", value);
        String message = param.filter(Sets.newHashSet());
        assertThat(message).isEqualTo("{k1=v1, k2=v2}");
    }
}
