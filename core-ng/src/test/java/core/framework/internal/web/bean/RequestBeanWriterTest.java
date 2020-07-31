package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class RequestBeanWriterTest {
    private RequestBeanWriter writer;
    private BeanClassValidator validator;

    @BeforeEach
    void createRequestBeanWriter() {
        validator = new BeanClassValidator();
        writer = new RequestBeanWriter();
    }

    @Test
    void register() {
        writer.registerBean(TestBean.class, validator);
        writer.registerBean(TestBean.class, validator);
    }

    @Test
    void registerQueryParam() {
        writer.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
        writer.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
    }
}
