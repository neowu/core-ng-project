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

    @Test
    void validateWithoutQueryParamAnnotation() {
        assertThatThrownBy(() -> new QueryParamClassValidator(TestQueryParamBeanWithoutQueryParam.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("field must have @QueryParam");
    }

    @Test
    void validateWithDuplicateQueryParamName() {
        assertThatThrownBy(() -> new QueryParamClassValidator(TestQueryParamBeanWithDuplicateQueryParamName.class, new BeanClassNameValidator()).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("found duplicate query param");
    }

    public static class TestQueryParamBeanWithPropertyAnnotation {
        @QueryParam(name = "name")
        @Property(name = "name")
        public String name;
    }

    public static class TestQueryParamBeanWithoutQueryParam {
        public String name;
    }

    public static class TestQueryParamBeanWithDuplicateQueryParamName {
        @QueryParam(name = "name")
        public String name1;
        @QueryParam(name = "name")
        public String name2;
    }
}
