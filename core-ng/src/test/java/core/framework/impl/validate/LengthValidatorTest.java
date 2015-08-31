package core.framework.impl.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
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

        Bean instance = new Bean();
        instance.field1 = "123456";
        instance.field2 = "1";

        ValidationErrors validationErrors = new ValidationErrors();
        validator.validate(instance, validationErrors);

        Assert.assertTrue(validationErrors.hasError());

        Map<String, String> errors = validationErrors.errors;

        assertEquals(2, errors.size());
        assertThat(errors.get("field1"), containsString("field1"));
        assertThat(errors.get("field2"), containsString("field2"));
    }
}