package core.framework.internal.validate.type;


import core.framework.internal.validate.ClassValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ClassValidatorTest {
    @Test
    void validateObjectClass() {
        assertThatThrownBy(() -> new ClassValidator(String.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new ClassValidator(int.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new ClassValidator(TestEnum.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");
    }

    enum TestEnum {
        A
    }
}
