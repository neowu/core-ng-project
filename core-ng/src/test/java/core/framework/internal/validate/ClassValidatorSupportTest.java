package core.framework.internal.validate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ClassValidatorSupportTest {
    private ClassValidatorSupport support;

    @BeforeEach
    void createClassValidatorSupport() {
        support = new ClassValidatorSupport();
    }

    @Test
    void validateField() {
        assertThatThrownBy(() -> support.validateField(TestBean.class.getDeclaredField("nonPublicField")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("field must be public");

        assertThatThrownBy(() -> support.validateField(TestBean.class.getDeclaredField("intField")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("primitive class is not supported");

        assertThatThrownBy(() -> support.validateField(TestBean.class.getDeclaredField("transientField")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("field must not be transient");

        assertThatThrownBy(() -> support.validateField(TestBean.class.getDeclaredField("finalField")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("field must not be final");

        assertThatThrownBy(() -> support.validateField(TestBean.class.getDeclaredField("staticField")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("field must not be static");
    }

    @Test
    void validateClass() {
        assertThatThrownBy(() -> support.validateClass(TestClassWithSuperClass.class))
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must not have super class");

        assertThatThrownBy(() -> support.validateClass(TestClassWithoutDefaultConstructor.class))
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must only have public default constructor");
    }

    public static class TestBean {
        @SuppressWarnings("PMD.MutableStaticState")
        static String staticField;

        static void setStaticField(String staticField) {
            TestBean.staticField = staticField;
        }

        @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
        @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
        public final String finalField = "value";
        public int intField;
        public transient String transientField;
        @SuppressFBWarnings("UUF_UNUSED_FIELD")
        String nonPublicField;
    }

    public static class TestClassWithSuperClass extends TestBean {
    }

    public static class TestClassWithoutDefaultConstructor {
        final String value;

        TestClassWithoutDefaultConstructor(String value) {
            this.value = value;
        }
    }
}
