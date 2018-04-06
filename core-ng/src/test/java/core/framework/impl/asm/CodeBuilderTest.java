package core.framework.impl.asm;

import core.framework.util.Lists;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CodeBuilderTest {
    @Test
    void appendCommaSeparatedValues() {
        CodeBuilder builder = new CodeBuilder();
        builder.appendCommaSeparatedValues(Lists.newArrayList("item1", "item2"));
        assertThat(builder.build()).isEqualTo("item1, item2");
    }
}
