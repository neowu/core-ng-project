package core.framework.impl.validate;

import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class BeanValidatorNotBlankTest {
    private BeanValidator validator;
    private BeanValidatorBuilder builder;

    @BeforeEach
    void createObjectValidator() {
        builder = new BeanValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().orElseThrow();
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
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-not-blank.java"), sourceCode);
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
        @NotBlank(message = "stringField1 must not be blank")
        public String stringField1;

        @NotBlank(message = "stringField2 must not be blank")
        public String stringField2;
    }
}
