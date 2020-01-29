package core.framework.internal.bean;

import core.framework.internal.reflect.Classes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassValidatorTest {
    @Test
    void validate() {
        new BeanClassValidator(TestBean.class, new BeanClassNameValidator()).validate();
    }

    @Test
    void validateWithList() {
        assertThatThrownBy(() -> new BeanClassValidator(List.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }

    @Test
    void validateWithDuplicateClassName() {
        var validator = new BeanClassNameValidator();
        validator.beanClasses.put(Classes.className(TestBean.class), Void.class);

        assertThatThrownBy(() -> new BeanClassValidator(TestBean.class, validator).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("found bean class with duplicate name which can be confusing");
    }

    @Test
    void validateWithDuplicateEnumName() {
        var validator = new BeanClassNameValidator();
        validator.beanClasses.put(Classes.className(TestBean.TestEnum.class), Void.class);

        assertThatThrownBy(() -> new BeanClassValidator(TestBean.class, validator).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("found bean class with duplicate name which can be confusing");
    }
}
