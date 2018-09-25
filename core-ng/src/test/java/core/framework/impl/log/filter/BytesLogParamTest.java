package core.framework.impl.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesLogParamTest {
    @Test
    void appendTruncatedValue() {
        var param = new BytesLogParam(Strings.bytes("text-♥"));
        var builder = new StringBuilder();
        param.append(builder, 6);

        assertThat(builder.toString()).isEqualTo("text-...(truncated)");
    }

    @Test
    void appendNull() {
        var param = new BytesLogParam(null);
        var builder = new StringBuilder();
        param.append(builder, 6);

        assertThat(builder.toString()).isEqualTo("null");
    }

    @Test
    void append() {
        var param = new BytesLogParam(Strings.bytes("value"));
        var builder = new StringBuilder();
        param.append(builder, 10);

        assertThat(builder.toString()).isEqualTo("value");
    }
}
