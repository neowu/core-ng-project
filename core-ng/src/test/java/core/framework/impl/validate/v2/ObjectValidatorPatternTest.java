package core.framework.impl.validate.v2;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class ObjectValidatorPatternTest {
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    public void valid() {
        Bean bean = new Bean();
        bean.field1 = "abc-def";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);
        assertFalse(errors.hasError());
    }

    @Test
    public void invalid() {
        Bean bean = new Bean();
        bean.field1 = "ABC-DEF";
        bean.field2 = "a001";
        bean.field3 = "A001";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("field3"), containsString("field3"));
    }

    static class Bean {
        @NotNull
        @Pattern(value = "[a-z-]+", message = "field1 must match pattern")
        public String field1;

        @Pattern("[a-z0-9]{0,20}")
        public String field2;

        @Pattern(value = "[a-z0-9]+", message = "field3 must be [a-z0-9]+")
        public String field3;
    }
}
