package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
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
public class PatternValidatorTest {
    Validator validator;

    @Before
    public void createValidator() {
        validator = new ValidatorBuilder(Bean.class, Field::getName).build();
    }

    @Test
    public void valid() {
        Bean bean = new Bean();
        bean.field1 = "abc-def";
        validator.validate(bean);
    }

    @Test
    public void invalid() {
        Bean bean = new Bean();
        bean.field1 = "ABC-DEF";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
    }

    static class Bean {
        @NotNull
        @Pattern(value = "[a-z-]+", message = "field1 must match pattern")
        public String field1;
    }
}