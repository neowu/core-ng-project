package core.framework.internal.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.Min;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanValidatorBuilderTest {
    @Test
    void withoutValidationAnnotation() {
        assertThat(new BeanValidatorBuilder(Bean.class).build()).isNotPresent();
    }

    @Test
    void validateMinAnnotation() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithInvalidMinAnnotation.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("@Min must on Number");
    }

    @Test
    void validateLengthAnnotation() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithInvalidLengthAnnotation.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("@Length must on String");
    }

    @Test
    void validateNotNull() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithDefaultValue.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("field with default value must have @NotNull");
    }

    public static class Bean {
        public String stringField;
    }

    public static class BeanWithInvalidMinAnnotation {
        @Min(1)
        public String stringField;
    }

    public static class BeanWithInvalidLengthAnnotation {
        @Length(min = 1)
        public Integer intField;
    }

    public static class BeanWithDefaultValue {
        public String stringField = "value";
    }
}
