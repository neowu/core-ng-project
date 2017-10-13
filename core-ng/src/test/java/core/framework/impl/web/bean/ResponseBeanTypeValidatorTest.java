package core.framework.impl.web.bean;

import core.framework.util.Types;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class ResponseBeanTypeValidatorTest {
    private ResponseBeanTypeValidator validator;

    @Before
    public void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator();
    }

    @Test
    public void listType() {
        validator.validate(Types.list(String.class));
        validator.validate(Types.list(TestBean.class));
    }

    @Test
    public void optionalType() {
        validator.validate(Types.optional(TestBean.class));
    }

    @Test
    public void beanType() {
        validator.validate(TestBean.class);
    }
}
