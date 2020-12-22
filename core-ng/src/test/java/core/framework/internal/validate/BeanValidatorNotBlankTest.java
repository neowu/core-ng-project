package core.framework.internal.validate;

import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorNotBlankTest {
    private BeanValidator validator;
    private BeanValidatorBuilder builder;

    @BeforeEach
    void createBeanValidator() {
        builder = new BeanValidatorBuilder(Bean.class);
        validator = builder.build();
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.stringField1 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(1);
        assertThat(errors.errors.get("stringField1")).contains("stringField1");
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualToIgnoringWhitespace(ClasspathResources.text("validator-test/validator-not-blank.java"));
    }

    @Test
    void partialValidate() {
        Bean bean = new Bean();
        bean.stringField2 = "";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(1);
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
