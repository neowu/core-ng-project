package core.framework.impl.web.bean;

import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ResponseBeanTypeValidatorTest {
    private ResponseBeanTypeValidator validator;

    @BeforeEach
    void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator(new BeanClassNameValidator());
    }

    @Test
    void listType() {
        assertThatThrownBy(() -> validator.validate(Types.list(String.class)))
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
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
