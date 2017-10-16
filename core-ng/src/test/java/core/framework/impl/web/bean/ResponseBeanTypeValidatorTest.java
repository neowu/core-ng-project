package core.framework.impl.web.bean;

import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class ResponseBeanTypeValidatorTest {
    private ResponseBeanTypeValidator validator;

    @BeforeEach
    void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator();
    }

    @Test
    void listType() {
        validator.validate(Types.list(String.class));
        validator.validate(Types.list(TestBean.class));
    }

    @Test
    void optionalType() {
        validator.validate(Types.optional(TestBean.class));
    }

    @Test
    void beanType() {
        validator.validate(TestBean.class);
    }
}
