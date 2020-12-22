package core.framework.internal.validate;

import core.framework.api.validate.Digits;
import core.framework.api.validate.Min;
import core.framework.api.validate.Size;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanValidatorBuilderTest {
    @Test
    void withoutValidationAnnotation() {
        assertThat(new BeanValidatorBuilder(Bean.class).build()).isNull();
    }

    @Test
    void validateMinAnnotation() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithInvalidMinAnnotation.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("@Min must on Number");
    }

    @Test
    void validateSizeAnnotation() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithInvalidSizeAnnotation.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("@Size must on String");
    }

    @Test
    void validateDigitsAnnotation() {
        assertThatThrownBy(() -> new BeanValidatorBuilder(BeanWithInvalidDigitsAnnotation.class).build())
                .isInstanceOf(Error.class)
                .hasMessageContaining("@Digits must on Number");
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

    public static class BeanWithInvalidSizeAnnotation {
        @Size(min = 1)
        public Integer intField;
    }

    public static class BeanWithInvalidDigitsAnnotation {
        @Digits(integer = 1)
        public String stringField;
    }

    public static class BeanWithDefaultValue {
        public String stringField = "value";
    }
}
