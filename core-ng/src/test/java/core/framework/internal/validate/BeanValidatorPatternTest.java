package core.framework.internal.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorPatternTest {
    private BeanValidator validator;

    @BeforeEach
    void createBeanValidator() {
        validator = new BeanValidatorBuilder(Bean.class).build();
    }

    @Test
    void valid() {
        var bean = new Bean();
        bean.field1 = "abc-def";

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);
        assertThat(errors.hasError()).isFalse();
    }

    @Test
    void invalid() {
        var bean = new Bean();
        bean.field1 = "ABC-DEF";
        bean.field2 = "a001";
        bean.field3 = "A001";

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(2);
        assertThat(errors.errors.get("field1")).isEqualTo("field must match /[a-z-]+/, value=ABC-DEF");
        assertThat(errors.errors.get("field3")).contains("field3");
    }

    static class Bean {
        @NotNull
        @Pattern("[a-z-]+")
        public String field1;

        @Pattern("[a-z0-9]{0,20}")
        public String field2;

        @Pattern(value = "[a-z0-9]+", message = "field3 must be [a-z0-9]+")
        public String field3;
    }
}
