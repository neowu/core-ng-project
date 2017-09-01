package core.framework.impl.validate.v2;

import core.framework.api.validate.NotEmpty;
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
public class ObjectValidatorNotEmptyTest {
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    public void validate() {
        Bean bean = new Bean();
        bean.stringField1 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("stringField1"), containsString("stringField1"));
    }

    @Test
    public void partialValidate() {
        Bean bean = new Bean();
        bean.stringField2 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("stringField2"), containsString("stringField2"));
    }

    static class Bean {
        @NotNull
        @NotEmpty(message = "stringField1 must not be empty")
        public String stringField1;

        @NotEmpty(message = "stringField2 must not be empty")
        public String stringField2;
    }
}
