package core.framework.internal.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TraceTest {
    @Test
    void parse() {
        assertThat(Trace.parse("false")).isEqualTo(Trace.NONE);
        assertThat(Trace.parse("true")).isEqualTo(Trace.CURRENT);

        assertThat(Trace.parse("cascade")).isEqualTo(Trace.CASCADE);
        assertThat(Trace.parse("CURRENT")).isEqualTo(Trace.CURRENT);
        assertThat(Trace.parse("none")).isEqualTo(Trace.NONE);
    }
}
