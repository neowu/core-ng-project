package core.framework.impl.web.response;

import core.framework.api.util.Lists;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.TestBean;
import org.junit.Test;

import java.util.List;

/**
 * @author neo
 */
public class BeanBodyResponseHandlerTest {
    BeanBodyResponseHandler handler = new BeanBodyResponseHandler(new BeanValidator());

    @Test
    public void validateList() {
        List<TestBean> list = Lists.newArrayList(new TestBean());
        handler.validateBeanClass(list);
    }

    @Test
    public void validateEmptyList() {
        List<TestBean> list = Lists.newArrayList();
        handler.validateBeanClass(list);
    }

    @Test
    public void validateBean() {
        TestBean bean = new TestBean();
        handler.validateBeanClass(bean);
    }
}