package core.framework.impl.web.response;

import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.bean.TestBean;
import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanBodyTest {
    private ResponseBeanTypeValidator validator;

    @BeforeEach
    void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator(new BeanClassNameValidator());
    }

    @Test
    void validateList() {
        List<TestBean> list = Lists.newArrayList();
        BeanBody body = new BeanBody(list);
        assertThatThrownBy(() -> body.validateBeanType(validator))
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
    }

    @Test
    void validateEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        BeanBody body = new BeanBody(optional);
        body.validateBeanType(validator);
    }

    @Test
    void validateBean() {
        TestBean bean = new TestBean();
        BeanBody body = new BeanBody(bean);
        body.validateBeanType(validator);
    }

    @Test
    void validateOptionalBean() {
        Optional<TestBean> optional = Optional.of(new TestBean());
        BeanBody body = new BeanBody(optional);
        body.validateBeanType(validator);
    }
}
