package core.framework.impl.validate;

import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class MinMaxValidatorTest {
    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.num1 = 0;
        bean.num2 = 11;

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("num1"), containsString("num1"));
        assertThat(errors.errors.get("num2"), containsString("num2"));
    }

    static class Bean {
        @Min(value = 1, message = "num1 must not be less than 1")
        public Integer num1;

        @Max(value = 10, message = "num2 must not be greater than 10")
        public Integer num2;
    }
}