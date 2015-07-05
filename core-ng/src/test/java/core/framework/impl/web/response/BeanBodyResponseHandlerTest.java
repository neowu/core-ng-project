package core.framework.impl.web.response;

import core.framework.api.util.Lists;
import core.framework.impl.web.TestBean;
import org.junit.Test;

import java.util.List;

/**
 * @author neo
 */
public class BeanBodyResponseHandlerTest {
    BeanBodyResponseHandler handler = new BeanBodyResponseHandler();

    @Test
    public void validateListBean() {
        List<TestBean> bean = Lists.newArrayList(new TestBean());
        handler.validateBeanClass(bean);
    }

    @Test
    public void validateBean() {
        TestBean bean = new TestBean();
        handler.validateBeanClass(bean);
    }
}