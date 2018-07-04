package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RequestBeanClassValidatorTest {
    @Test
    void validate() {
        new RequestBeanClassValidator(TestBean.class, new BeanClassNameValidator()).validate();

        assertThatThrownBy(() -> new RequestBeanClassValidator(List.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
    }
}
