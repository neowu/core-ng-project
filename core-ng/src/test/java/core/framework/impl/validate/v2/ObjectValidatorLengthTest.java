package core.framework.impl.validate.v2;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class ObjectValidatorLengthTest {
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    public void validate() {
        Bean bean = new Bean();
        bean.field1 = "123456";
        bean.field2 = "1";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("field2"), containsString("field2"));
    }

    @Test
    public void partialValidate() {
        Bean bean = new Bean();
        bean.field1 = "123456";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
    }

    static class Bean {
        @NotNull
        @Length(max = 5, message = "field1 must not be longer than 5")
        public String field1;
        @NotNull
        @Length(min = 5, message = "field2 must be longer than 5")
        public String field2;
        @Length(min = 5, message = "field3 must be longer than 5")
        public String field3;
    }
}
