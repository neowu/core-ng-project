package core.framework.impl.validate;

import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ObjectValidatorMinMaxTest {
    private ObjectValidator validator;

    @BeforeEach
    void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.num1 = 0;
        bean.num2 = 11;

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("num1")).contains("num1");
        assertThat(errors.errors.get("num2")).contains("num2");
    }

    @Test
    void validateWithoutError() {
        Bean bean = new Bean();
        bean.num1 = 1;
        bean.num2 = 10;
        bean.num3 = new BigDecimal(6);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertFalse(errors.hasError());
    }

    static class Bean {
        @Min(value = 1, message = "num1 must not be less than 1")
        public Integer num1;

        @Max(value = 10, message = "num2 must not be greater than 10")
        public Integer num2;

        @Min(5)
        @Max(10)
        public BigDecimal num3;
    }
}
