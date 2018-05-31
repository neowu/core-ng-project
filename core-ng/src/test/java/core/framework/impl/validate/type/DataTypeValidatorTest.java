package core.framework.impl.validate.type;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DataTypeValidatorTest {
    @Test
    void validateObjectClass() {
        assertThatThrownBy(() -> new DataTypeValidator(String.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new DataTypeValidator(int.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");

        assertThatThrownBy(() -> new DataTypeValidator(TestEnum.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("class must be bean class");
    }

    enum TestEnum {
        A
    }
}
