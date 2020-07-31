package core.framework.internal.asm;

import core.framework.http.HTTPMethod;
import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LiteralTest {
    @Test
    void enumVariable() {
        assertThat(Literal.variable(HTTPMethod.POST)).isEqualTo("core.framework.http.HTTPMethod.POST");
    }

    @Test
    void typeVariable() {
        assertThat(Literal.variable(String.class)).isEqualTo("java.lang.String.class");

        assertThat(Literal.variable(Types.list(String.class))).isEqualTo("core.framework.util.Types.list(java.lang.String.class)");

        assertThat(Literal.variable(Types.optional(String.class))).isEqualTo("core.framework.util.Types.optional(java.lang.String.class)");
    }

    @Test
    void stringVariable() {
        assertThat(Literal.variable("\n")).isEqualTo("\"\\n\"");
        assertThat(Literal.variable("\r")).isEqualTo("\"\\r\"");
        assertThat(Literal.variable("\\")).isEqualTo("\"\\\\\"");
        assertThat(Literal.variable("\"")).isEqualTo("\"\\\"\"");
    }
}
