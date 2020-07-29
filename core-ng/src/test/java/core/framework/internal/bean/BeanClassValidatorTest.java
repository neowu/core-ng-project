package core.framework.internal.bean;

import core.framework.internal.reflect.Classes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassValidatorTest {
    private BeanClassValidator validator;

    @BeforeEach
    void createBeanClassValidator() {
        validator = new BeanClassValidator();
    }

    @Test
    void validate() {
        validator.validate(TestBean.class);
    }

    @Test
    void validateWithList() {
        assertThatThrownBy(() -> validator.validate(List.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }

    @Test
    void validateWithDuplicateClassName() {
        validator.beanClassNameValidator.beanClasses.put(Classes.className(TestBean.class), Void.class);

        assertThatThrownBy(() -> validator.validate(TestBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found bean class with duplicate name which can be confusing");
    }

    @Test
    void validateWithDuplicateEnumName() {
        validator.beanClassNameValidator.beanClasses.put(Classes.className(TestBean.TestEnum.class), Void.class);

        assertThatThrownBy(() -> validator.validate(TestBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found bean class with duplicate name which can be confusing");
    }
}
