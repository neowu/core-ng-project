package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class QueryParamBeanTypeValidatorTest {
    @Test
    void beanType() {
        new QueryParamBeanTypeValidator(TestQueryParamBean.class).validate();
    }
}
