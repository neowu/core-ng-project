package core.framework.impl.validate;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObjectValidatorLengthTest {
    private ObjectValidatorBuilder builder;
    private ObjectValidator validator;

    @BeforeAll
    void createObjectValidator() {
        builder = new ObjectValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().get();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-length.java"), sourceCode);
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.field1 = "123456";
        bean.field2 = "1";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1")).contains("field1");
        assertThat(errors.errors.get("field2")).contains("field2");
    }

    @Test
    void partialValidate() {
        Bean bean = new Bean();
        bean.field1 = "123456";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field1")).contains("field1");
    }

    @Test
    void validateWithoutError() {
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
