package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassValidatorTest {
    @Test
    void validate() {
        var beanClassNameValidator = new BeanClassNameValidator();
        new BeanClassValidator(TestBean.class, beanClassNameValidator).validate();

        assertThatThrownBy(() -> new BeanClassValidator(List.class, beanClassNameValidator).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }
}
