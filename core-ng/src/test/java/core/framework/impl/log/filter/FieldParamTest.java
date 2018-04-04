package core.framework.impl.log.filter;

import core.framework.util.Maps;
import core.framework.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FieldParamTest {
    @Test
    void filter() {
        FieldParam param = new FieldParam("SessionId", "value");
        String message = param.filter(Sets.newHashSet("SessionId"));
        assertThat(message).isNotEqualTo("value");
    }

    @Test
    void formatArrayValue() {
        FieldParam param = new FieldParam("key", new Object[]{1, 2, 3});
        String message = param.filter(Sets.newHashSet());
        assertThat(message).isEqualTo("[1, 2, 3]");
    }

    @Test
    void formatMapValue() {
        Map<String, String> value = Maps.newHashMap("k1", "v1");
        value.put("k2", "v2");
        FieldParam param = new FieldParam("key", value);
        String message = param.filter(Sets.newHashSet());
        assertThat(message).isEqualTo("{k1=v1, k2=v2}");
    }
}
