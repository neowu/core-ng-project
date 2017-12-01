package core.framework.impl.validate;

import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ObjectValidatorNotEmptyTest {
    private ObjectValidator validator;

    @BeforeEach
    void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.stringField1 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("stringField1")).contains("stringField1");
    }

    @Test
    void partialValidate() {
        Bean bean = new Bean();
        bean.stringField2 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("stringField2")).contains("stringField2");
    }

    static class Bean {
        @NotNull
        @NotEmpty(message = "stringField1 must not be empty")
        public String stringField1;

        @NotEmpty(message = "stringField2 must not be empty")
        public String stringField2;
    }
}
