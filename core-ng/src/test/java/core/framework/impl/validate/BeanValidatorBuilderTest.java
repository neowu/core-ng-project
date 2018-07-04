package core.framework.impl.validate;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorBuilderTest {
    @Test
    void withoutValidationAnnotation() {
        assertThat(new BeanValidatorBuilder(Bean.class, Field::getName).build()).isNotPresent();
    }

    public static class Bean {
        public String stringField;
    }
}
