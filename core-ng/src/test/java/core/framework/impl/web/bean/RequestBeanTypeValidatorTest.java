package core.framework.impl.web.bean;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RequestBeanTypeValidatorTest {
    @Test
    void listType() {
        new RequestBeanTypeValidator(Types.list(String.class)).validate();
        new RequestBeanTypeValidator(Types.list(TestBean.class)).validate();
    }

    @Test
    void optionalType() {
        assertThatThrownBy(() -> new RequestBeanTypeValidator(Types.optional(TestBean.class)).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level optional is not allowed");
    }

    @Test
    void beanType() {
        new RequestBeanTypeValidator(TestBean.class).validate();
    }
}
