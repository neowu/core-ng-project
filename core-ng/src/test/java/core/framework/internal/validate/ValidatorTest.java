package core.framework.internal.validate;

import core.framework.api.validate.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ValidatorTest {
    private Validator<TestBean> validator;

    @BeforeEach
    void createValidator() {
        validator = Validator.of(TestBean.class);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> validator.validate(null, false))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("bean must not be null");

        assertThatThrownBy(() -> validator.validate(new TestBean(), false))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("value=field must not be null")
            .hasMessageContaining("array=field must not be null")
            .hasMessageContaining("lock=field must not be null");
    }

    public static class TestBean {
        @NotNull
        public String value;

        @NotNull
        public String[] array;

        @NotNull
        public Object lock;
    }
}
