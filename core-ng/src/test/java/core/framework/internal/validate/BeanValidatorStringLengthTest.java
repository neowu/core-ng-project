package core.framework.internal.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanValidatorStringLengthTest {
    private BeanValidatorBuilder builder;
    private BeanValidator validator;

    @BeforeAll
    void createBeanValidator() {
        builder = new BeanValidatorBuilder(Bean.class);
        validator = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualToIgnoringWhitespace(ClasspathResources.text("validator-test/validator-length.java"));
    }

    @Test
    void validate() {
        var bean = new Bean();
        bean.field1 = "123456";
        bean.field2 = "1";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(2);
        assertThat(errors.errors.get("field1")).isEqualTo("field1 must not be longer than 5");
        assertThat(errors.errors.get("field2")).contains("field2");
    }

    @Test
    void partialValidate() {
        var bean = new Bean();
        bean.field1 = "123456";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(1);
        assertThat(errors.errors.get("field1")).contains("field1");
    }

    @Test
    void validateWithoutError() {
        var bean = new Bean();
        bean.field1 = "12345";
        bean.field2 = "12345";
        bean.field3 = "123";

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @NotNull
        @Size(max = 5, message = "field1 must not be longer than {max}")
        public String field1;
        @NotNull
        @Size(min = 5, message = "field2 must be longer than {min}")
        public String field2;
        @Size(min = 3, max = 5)
        public String field3;
    }
}
