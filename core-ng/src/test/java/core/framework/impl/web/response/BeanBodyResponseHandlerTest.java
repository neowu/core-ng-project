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
public class BeanBodyResponseHandlerTest {
    private BeanBodyResponseHandler handler;

    @Before
    public void createBeanBodyResponseHandler() {
        handler = new BeanBodyResponseHandler(new ResponseBeanTypeValidator());
    }

    @Test
    public void validateList() {
        List<TestBean> list = Lists.newArrayList(new TestBean());
        handler.validateBeanType(list);
    }

    @Test
    public void validateEmptyList() {
        List<TestBean> list = Lists.newArrayList();
        handler.validateBeanType(list);
    }

    @Test
    public void validateEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        handler.validateBeanType(optional);
    }

    @Test
    public void validateBean() {
        TestBean bean = new TestBean();
        handler.validateBeanType(bean);
    }

    @Test
    public void validateOptionalBean() {
        Optional<TestBean> optional = Optional.of(new TestBean());
        handler.validateBeanType(optional);
    }
}
