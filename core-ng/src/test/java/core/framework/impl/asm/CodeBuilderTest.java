package core.framework.impl.asm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CodeBuilderTest {
    @Test
    void appendCommaSeparatedValues() {
        CodeBuilder builder = new CodeBuilder();
        builder.appendCommaSeparatedValues(List.of("item1", "item2"));
        assertThat(builder.build()).isEqualTo("item1, item2");
    }
}
