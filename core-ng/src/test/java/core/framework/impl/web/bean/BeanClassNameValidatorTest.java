package core.framework.impl.web.bean;

import core.framework.impl.reflect.Classes;
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
    void validateBeanClass() {
        validator.registeredClasses.put(Classes.className(TestBean.class), Void.class);

        assertThatThrownBy(() -> validator.validateBeanClass(TestBean.class))
                .isInstanceOf(Error.class).hasMessageContaining("found bean class with duplicate name");
    }

    public static class TestBean {
    }
}
