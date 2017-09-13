package core.framework.impl.web.response;

import core.framework.api.util.Lists;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.bean.TestBean;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class BeanBodyTest {
    private ResponseBeanTypeValidator validator;

    @Before
    public void createResponseBeanTypeValidator() {
        validator = new ResponseBeanTypeValidator();
    }

    @Test
    public void validateList() {
        List<TestBean> list = Lists.newArrayList(new TestBean());
        BeanBody body = new BeanBody(list);
        body.validateBeanType(validator);
    }

    @Test
    public void validateEmptyList() {
        List<TestBean> list = Lists.newArrayList();
        BeanBody body = new BeanBody(list);
        body.validateBeanType(validator);
    }

    @Test
    public void validateEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        BeanBody body = new BeanBody(optional);
        body.validateBeanType(validator);
    }

    @Test
    public void validateBean() {
        TestBean bean = new TestBean();
        BeanBody body = new BeanBody(bean);
        body.validateBeanType(validator);
    }

    @Test
    public void validateOptionalBean() {
        Optional<TestBean> optional = Optional.of(new TestBean());
        BeanBody body = new BeanBody(optional);
        body.validateBeanType(validator);
    }
}
