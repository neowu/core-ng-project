package core.framework.internal.http;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FormLogParamTest {
    @Test
    void filterWithoutMask() {
        var param = new FormLogParam(null);
        var value = "a=1&b=2";
        assertThat(param.filter(value, Set.of()).toString()).isEqualTo(value);
    }

    @Test
    void filterWithMask() {
        var param = new FormLogParam(null);
        assertThat(param.filter("password1=1+2&password2=123", Set.of("password1", "password2")).toString())
                .isEqualTo("password1=******&password2=******");

        assertThat(param.filter("user=abc&password=123", Set.of("password", "code")).toString())
                .isEqualTo("user=abc&password=******");
    }

    @Test
    void appendWithTruncation() {
        var param = new FormLogParam(Strings.bytes("user=user&password=123456"));
        var builder = new StringBuilder();
        param.append(builder, Set.of("password"), 24);
        assertThat(builder.toString()).isEqualTo("user=user&password=******...(truncated)");

        builder = new StringBuilder();
        param.append(builder, Set.of("password"), 12);
        assertThat(builder.toString()).isEqualTo("user=user&p...(truncated)");
    }
}
