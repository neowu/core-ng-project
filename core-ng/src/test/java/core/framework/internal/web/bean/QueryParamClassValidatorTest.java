package core.framework.internal.web.bean;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class QueryParamClassValidatorTest {
    @Test
    void validate() {
        new QueryParamClassValidator(TestQueryParamBean.class, new BeanClassNameValidator()).validate();
    }
}
