package core.framework.impl.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class LengthValidatorTest {
    static class Bean {
        @NotNull
        @Length(max = 5, message = "field1 must not be longer than 5")
        public String field1;
        @NotNull
        @Length(min = 5, message = "field2 must be longer than 5")
        public String field2;
        @Length(min = 5, message = "optionalField1 must be longer than 5")
        public String optionalField1;
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.field1 = "123456";
        bean.field2 = "1";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("field2"), containsString("field2"));
    }

    @Test
    public void partialValidate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.field1 = "123456";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        Assert.assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
    }
}