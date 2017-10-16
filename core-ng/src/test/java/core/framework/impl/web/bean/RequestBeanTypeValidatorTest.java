package core.framework.impl.web.bean;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Error error = assertThrows(Error.class, () -> new RequestBeanTypeValidator(Types.optional(TestBean.class)).validate());
        assertTrue(error.getMessage().startsWith("top level optional is not allowed"));
    }

    @Test
    void beanType() {
        new RequestBeanTypeValidator(TestBean.class).validate();
    }
}
