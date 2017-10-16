package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.TestBean;
import core.framework.impl.web.bean.TestQueryParamBean;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RequestImplTest {
    private RequestImpl request;

    @BeforeEach
    void createRequest() {
        request = new RequestImpl(null, new RequestBeanMapper());
    }

    @Test
    void beanWithGet() {
        request.method = HTTPMethod.GET;
        request.queryParams.put("int_field", "1");

        TestQueryParamBean bean = request.bean(TestQueryParamBean.class);
        assertEquals(Integer.valueOf(1), bean.intField);
    }

    @Test
    void beanWithJsonPost() {
        request.method = HTTPMethod.POST;
        request.contentType = ContentType.APPLICATION_JSON;
        request.body = Strings.bytes("{\"big_decimal_field\": 1}");

        TestBean bean = request.bean(TestBean.class);
        assertEquals(BigDecimal.valueOf(1), bean.bigDecimalField);
    }

    @Test
    void beanWithFormPost() {
        request.method = HTTPMethod.POST;
        request.formParams.put("long_field", "1");

        TestQueryParamBean bean = request.bean(TestQueryParamBean.class);
        assertEquals(Long.valueOf(1), bean.longField);
    }
}
