package core.framework.internal.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.internal.bean.BeanClassNameValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class QueryParamClassValidatorTest {
    @Test
    void validate() {
        new QueryParamClassValidator(TestQueryParamBean.class, new BeanClassNameValidator()).validate();
    }

    @Test
    void validateWithPropertyAnnotation() {
        assertThatThrownBy(() -> new QueryParamClassValidator(TestQueryParamBeanWithPropertyAnnotation.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("field must not have @Property");
    }

    public static class TestQueryParamBeanWithPropertyAnnotation {
        @QueryParam(name = "name")
        @Property(name = "name")
        public String name;
    }
}
