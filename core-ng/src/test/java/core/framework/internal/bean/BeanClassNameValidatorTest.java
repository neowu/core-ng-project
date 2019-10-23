package core.framework.internal.bean;

import core.framework.internal.reflect.Classes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassNameValidatorTest {
    private BeanClassNameValidator validator;

    @BeforeEach
    void createBeanClassNameValidator() {
        validator = new BeanClassNameValidator();
    }

    @Test
    void validate() {
        validator.beanClasses.put(Classes.className(TestBean.class), Void.class);

        assertThatThrownBy(() -> validator.validate(TestBean.class))
            .isInstanceOf(Error.class).hasMessageContaining("found bean class with duplicate name");
    }
}
