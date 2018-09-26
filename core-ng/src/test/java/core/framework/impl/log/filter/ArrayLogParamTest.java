package core.framework.impl.log.filter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ArrayLogParamTest {
    @Test
    void append() {
        var param = new ArrayLogParam("1", "2", "3");
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString())
                .isEqualTo("[1, 2, 3]");
    }

    @Test
    void appendWithTruncation() {
        var param = new ArrayLogParam("1", "2", "3");
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 5);
        assertThat(builder.toString())
                .isEqualTo("[1, 2...(truncated)");
    }
}
