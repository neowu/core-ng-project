package core.framework.internal.validate;

import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorMinMaxTest {
    private BeanValidator validator;

    @BeforeEach
    void createBeanValidator() {
        validator = new BeanValidatorBuilder(Bean.class).build();
    }

    @Test
    void validate() {
        var bean = new Bean();
        bean.num1 = 0;
        bean.num2 = 11;

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(2);
        assertThat(errors.errors.get("num1")).isEqualTo("num1 must not be less than 1.0, value=0");
        assertThat(errors.errors.get("num2")).isEqualTo("num2 must not be greater than 10.0, value=11");
    }

    @Test
    void validateWithoutError() {
        var bean = new Bean();
        bean.num1 = 1;
        bean.num2 = 10;
        bean.num3 = new BigDecimal(6);

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @Min(value = 1, message = "num1 must not be less than {min}, value={value}")
        public Integer num1;

        @Max(value = 10, message = "num2 must not be greater than {max}, value={value}")
        public Integer num2;

        @Min(5)
        @Max(10)
        public BigDecimal num3;
    }
}
