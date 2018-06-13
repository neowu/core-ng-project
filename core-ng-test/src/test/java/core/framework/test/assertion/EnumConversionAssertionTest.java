package core.framework.test.assertion;

import org.junit.jupiter.api.Test;

import static core.framework.test.assertion.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class EnumConversionAssertionTest {
    @Test
    void hasExactlyEnumConstants() {
        assertThat(Enum1.class).hasExactlyEnumConstants(Enum2.class);
    }

    @Test
    void failWithMessage() {
        assertThatThrownBy(() -> assertThat(Enum1.class).hasExactlyEnumConstants(Enum3.class))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("has exactly constants of")
                .hasMessageContaining("<[C]>");
    }

    public enum Enum1 {
        A,
        B,
    }

    public enum Enum2 {
        A,
        B,
    }

    public enum Enum3 {
        A,
        B,
        C,
    }
}
