package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ObjectValidatorPatternTest {
    private ObjectValidator validator;

    @BeforeEach
    void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    void valid() {
        Bean bean = new Bean();
        bean.field1 = "abc-def";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);
        assertFalse(errors.hasError());
    }

    @Test
    void invalid() {
        Bean bean = new Bean();
        bean.field1 = "ABC-DEF";
        bean.field2 = "a001";
        bean.field3 = "A001";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1")).contains("field1");
        assertThat(errors.errors.get("field3")).contains("field3");
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
