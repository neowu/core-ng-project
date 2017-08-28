package core.framework.impl.web.bean;

import core.framework.api.util.Types;
import org.junit.Test;

/**
 * @author neo
 */
public class ResponseBeanTypeValidatorTest {
    @Test
    public void listType() {
        new ResponseBeanTypeValidator(Types.list(String.class)).validate();
        new ResponseBeanTypeValidator(Types.list(TestBean.class)).validate();
    }

    @Test
    public void optionalType() {
        new ResponseBeanTypeValidator(Types.optional(TestBean.class)).validate();
    }

    @Test
    public void beanType() {
        new ResponseBeanTypeValidator(TestBean.class).validate();
    }
}
