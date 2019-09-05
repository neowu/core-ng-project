package core.framework.internal.log.filter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FieldLogParamTest {
    @Test
    void append() {
        var param = new FieldLogParam("SessionId", "value");

        var builder = new StringBuilder();
        param.append(builder, Set.of("SessionId"), 1000);
        assertThat(builder.toString()).isNotEqualTo("value");

        builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("value");
    }
}
