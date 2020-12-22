package core.framework.internal.validate;

import core.framework.api.validate.Digits;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author chris
 */
class BeanValidatorDigitsTest {
    private BeanValidator validator;
    private BeanValidatorBuilder builder;

    @BeforeEach
    void createBeanValidator() {
        builder = new BeanValidatorBuilder(Bean.class);
        validator = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualToIgnoringWhitespace(ClasspathResources.text("validator-test/validator-digits.java"));
    }

    @Test
    void validate() {
        var bean = new Bean();
        bean.num1 = 10;
        bean.num2 = 11;
        bean.num3 = 1D / 3D;
        bean.num4 = new BigDecimal("3.14");

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(4)
                .containsEntry("num1", "field out of bounds (<1 digits>.<inf digits> expected), value=10")
                .containsEntry("num2", "num2 out of bounds. expected(<1 digits>.<2 digits>), actual value=11")
                .containsEntry("num3", "field out of bounds (<1 digits>.<2 digits> expected), value=0.3333333333333333")
                .containsEntry("num4", "field out of bounds (<2 digits>.<0 digits> expected), value=3.14");
    }

    @Test
    void validateWithoutError() {
        var bean = new Bean();
        bean.num1 = 1;
        bean.num2 = 9;
        bean.num3 = 3.76D;
        bean.num4 = new BigDecimal("99");

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @Digits(integer = 1)
        public Integer num1;

        @Digits(integer = 1, fraction = 2, message = "num2 out of bounds. expected(<{integer} digits>.<{fraction} digits>), actual value={value}")
        public Integer num2;

        @Digits(integer = 1, fraction = 2)
        public Double num3;

        @Digits(integer = 2, fraction = 0)
        public BigDecimal num4;
    }
}
