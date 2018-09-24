package core.framework.impl.log.filter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FieldLogParamTest {
    @Test
    void filter() {
        var param = new FieldLogParam("SessionId", "value");
        assertThat(param.filter(Set.of("SessionId"))).isNotEqualTo("value");

        assertThat(param.filter(Set.of())).isEqualTo("value");
    }
}
