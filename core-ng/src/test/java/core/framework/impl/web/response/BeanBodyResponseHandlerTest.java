package core.framework.impl.web.response;

import core.framework.api.util.Lists;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.TestBean;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author neo
 */
public class BeanBodyResponseHandlerTest {
    private BeanBodyResponseHandler handler;

    @Before
    public void createBeanBodyResponseHandler() {
        handler = new BeanBodyResponseHandler(new BeanValidator());
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
    public void validateBean() {
        TestBean bean = new TestBean();
        handler.validateBeanType(bean);
    }
}