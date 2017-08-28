package core.framework.impl.web.bean;

import core.framework.api.web.service.QueryParam;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author neo
 */
public class QueryParamBeanTypeValidatorTest {
    @Test
    public void beanType() {
        new QueryParamBeanTypeValidator(TestQueryParamBean.class).validate();
    }

    public static class TestQueryParamBean {
        @QueryParam(name = "int_field")
        public Integer intField;

        @QueryParam(name = "string_field")
        public String stringField;

        @QueryParam(name = "date_time_field")
        public LocalDateTime dateTimeField;
    }
}
