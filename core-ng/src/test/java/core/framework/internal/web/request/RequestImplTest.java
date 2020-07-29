package core.framework.internal.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.internal.web.bean.TestQueryParamBean;
import core.framework.util.Strings;
import core.framework.web.CookieSpec;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RequestImplTest {
    private RequestImpl request;

    @BeforeEach
    void createRequest() {
        var beanClassNameValidator = new BeanClassNameValidator();
        var reader = new RequestBeanReader();
        reader.registerQueryParam(TestQueryParamBean.class, beanClassNameValidator);
        reader.registerBean(TestBean.class, beanClassNameValidator);
        request = new RequestImpl(null, reader);
    }

    @Test
    void beanWithGet() {
        request.method = HTTPMethod.GET;
        request.queryParams.put("int_field", "1");

        TestQueryParamBean bean = request.bean(TestQueryParamBean.class);
        assertThat(bean.intField).isEqualTo(1);
    }

    @Test
    void beanWithJSONPost() {
        request.method = HTTPMethod.POST;
        request.contentType = ContentType.APPLICATION_JSON;
        request.body = Strings.bytes("{\"big_decimal_field\": 1, \"int_field\": 3}");

        TestBean bean = request.bean(TestBean.class);
        assertThat(bean.bigDecimalField).isEqualTo("1");
    }

    @Test
    void beanWithFormPost() {
        request.method = HTTPMethod.POST;
        request.formParams.put("long_field", "1");

        TestQueryParamBean bean = request.bean(TestQueryParamBean.class);
        assertThat(bean.longField).isEqualTo(1);
    }

    @Test
    void beanWithValidationError() {
        request.method = HTTPMethod.POST;
        request.contentType = ContentType.APPLICATION_JSON;
        request.body = Strings.bytes("{\"big_decimal_field\": 1}");
        assertThatThrownBy(() -> request.bean(TestBean.class))
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> assertThat(((BadRequestException) exception).errorCode()).isEqualTo("VALIDATION_ERROR"));
    }

    @Test
    void cookie() {
        CookieSpec spec = new CookieSpec("name");
        assertThat(request.cookie(spec)).isNotPresent();

        request.cookies = Map.of("name", "value");
        assertThat(request.cookie(spec)).get().isEqualTo("value");
    }
}
