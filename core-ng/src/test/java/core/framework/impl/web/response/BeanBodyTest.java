package core.framework.impl.web.response;

import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.bean.TestBean;
import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
class BeanBodyTest {
    private ResponseBeanTypeValidator validator;

    @BeforeEach
    void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator();
    }

    @Test
    void validateList() {
        List<TestBean> list = Lists.newArrayList(new TestBean());
        BeanBody body = new BeanBody(list);
        body.validateBeanType(validator);
    }

    @Test
    void validateEmptyList() {
        List<TestBean> list = Lists.newArrayList();
        BeanBody body = new BeanBody(list);
        body.validateBeanType(validator);
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
