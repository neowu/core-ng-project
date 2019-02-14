package core.framework.internal.validate.type;


import core.framework.internal.validate.BeanClassValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassValidatorTest {
    @Test
    void validateObjectClass() {
        assertThatThrownBy(() -> new BeanClassValidator(String.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new BeanClassValidator(int.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new BeanClassValidator(TestEnum.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");
    }

    enum TestEnum {
        A
    }
}
