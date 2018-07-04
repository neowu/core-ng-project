package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class QueryParamBeanClassValidatorTest {
    @Test
    void validate() {
        new QueryParamBeanClassValidator(TestQueryParamBean.class, new BeanClassNameValidator()).validate();
    }
}
