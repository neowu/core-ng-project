package core.framework.test.assertion;

import core.framework.test.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class EnumConversionAssertTest {
    @Test
    void hasExactlyConstantsAs() {
        Assertions.assertEnumClass(Enum1.class).hasExactlyConstantsAs(Enum2.class);
    }

    @Test
    void failWithMessage() {
        assertThatThrownBy(() -> Assertions.assertEnumClass(Enum1.class).hasExactlyConstantsAs(Enum3.class))
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
