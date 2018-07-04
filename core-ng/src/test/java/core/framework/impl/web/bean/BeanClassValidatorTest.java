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
        new BeanClassValidator(TestBean.class, new BeanClassNameValidator()).validate();

        assertThatThrownBy(() -> new BeanClassValidator(List.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
    }
}
