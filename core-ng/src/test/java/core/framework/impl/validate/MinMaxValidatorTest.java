package core.framework.impl.validate;

import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class MinMaxValidatorTest {
    static class TestBean {
        @Min(value = 1, message = "num1 must not be less than 1")
        public Integer num1;

        @Max(value = 10, message = "num2 must not be greater than 10")
        public Integer num2;
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(TestBean.class, Field::getName).build();

        TestBean bean = new TestBean();
        bean.num1 = 0;
        bean.num2 = 11;

        ValidationErrors validationErrors = new ValidationErrors();
        validator.validate(bean, validationErrors);

        Assert.assertTrue(validationErrors.hasError());

        Map<String, String> errors = validationErrors.errors;
        assertEquals(2, errors.size());
        assertThat(errors.get("num1"), containsString("num1"));
        assertThat(errors.get("num2"), containsString("num2"));
    }
}