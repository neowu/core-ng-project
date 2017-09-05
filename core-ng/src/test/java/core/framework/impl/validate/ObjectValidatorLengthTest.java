package core.framework.impl.validate;

import core.framework.api.util.ClasspathResources;
import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
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
public class ObjectValidatorLengthTest {
    ObjectValidatorBuilder builder;
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        builder = new ObjectValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().get();
    }

    @Test
    public void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-length.java"), sourceCode);
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

    @Test
    public void validateWithoutError() {
        Bean bean = new Bean();
        bean.field1 = "12345";
        bean.field2 = "12345";
        bean.field3 = "123";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertFalse(errors.hasError());
    }

    static class Bean {
        @NotNull
        @Length(max = 5, message = "field1 must not be longer than 5")
        public String field1;
        @NotNull
        @Length(min = 5, message = "field2 must be longer than 5")
        public String field2;
        @Length(min = 3, max = 5, message = "field3 length must between 3 and 5")
        public String field3;
    }
}
