package core.framework.impl.web.bean;

import core.framework.api.util.Types;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class RequestBeanTypeValidatorTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void listType() {
        new RequestBeanTypeValidator(Types.list(String.class)).validate();
        new RequestBeanTypeValidator(Types.list(TestBean.class)).validate();
    }

    @Test
    public void optionalType() {
        exception.expectMessage("top level optional is not allowed");

        new RequestBeanTypeValidator(Types.optional(TestBean.class)).validate();
    }

    @Test
    public void beanType() {
        new RequestBeanTypeValidator(TestBean.class).validate();
    }
}
