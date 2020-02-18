package core.framework.test.assertion;

import org.junit.jupiter.api.Test;

import static core.framework.test.Assertions.assertEnumClass;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class EnumConversionAssertTest {
    @Test
    void hasExactlyConstantsAs() {
        assertEnumClass(Enum1.class).hasExactlyConstantsAs(Enum2.class);

        assertThatThrownBy(() -> assertEnumClass(Enum1.class).hasExactlyConstantsAs(Enum3.class))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("has exactly constants of")
                .hasMessageContaining("<[C]>");
    }

    @Test
    void hasAllConstantsOf() {
        assertEnumClass(Enum1.class).hasAllConstantsOf(Enum2.class);
        assertEnumClass(Enum3.class).hasAllConstantsOf(Enum2.class);

        assertThatThrownBy(() -> assertEnumClass(Enum1.class).hasAllConstantsOf(Enum3.class))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("has all constants of")
                .hasMessageContaining("<[C]>");
    }

    public enum Enum1 {
        A,
        B
    }

    public enum Enum2 {
        A,
        B
    }

    public enum Enum3 {
        A,
        B,
        C
    }
}
