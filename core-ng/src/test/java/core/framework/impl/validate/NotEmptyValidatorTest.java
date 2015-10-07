package core.framework.impl.validate;

import core.framework.api.validate.NotEmpty;
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
public class NotEmptyValidatorTest {
    static class Bean {
        @NotNull
        @NotEmpty(message = "field1 must not be empty")
        public String field1;

        @NotEmpty(message = "optionalField1 must not be empty")
        public String optionalField1;
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.field1 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
    }
}