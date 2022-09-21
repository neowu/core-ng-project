package core.framework.internal.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.internal.web.bean.TestQueryParamBean;
import core.framework.util.Strings;
import core.framework.web.CookieSpec;
import core.framework.web.exception.BadRequestException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class RequestImplTest {
    @Mock
    HttpServerExchange exchange;
    private RequestImpl request;

    @BeforeEach
    void createRequest() {
        var validator = new BeanClassValidator();
        var reader = new RequestBeanReader();
        reader.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
        reader.registerBean(TestBean.class, validator);
        request = new RequestImpl(exchange, reader);
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
    void beanWithInvalidJSON() {
        request.method = HTTPMethod.POST;
        request.contentType = ContentType.APPLICATION_JSON;
        request.body = Strings.bytes("{\"big_decimal_field\": 1");
        assertThatThrownBy(() -> request.bean(TestBean.class))
            .isInstanceOf(BadRequestException.class)
            .satisfies(exception -> {
                assertThat(exception.getMessage()).startsWith("failed to deserialize request");
                assertThat(((BadRequestException) exception).errorCode()).isEqualTo("INVALID_HTTP_REQUEST");
            });
    }

    @Test
    void cookie() {
        CookieSpec spec = new CookieSpec("name");
        assertThat(request.cookie(spec)).isNotPresent();

        request.cookies = Map.of("name", "value");
        assertThat(request.cookie(spec)).get().isEqualTo("value");
    }

    @Test
    void session() {
        request.scheme = "http";

        assertThatThrownBy(() -> request.session())
            .isInstanceOf(Error.class)
            .hasMessageContaining("session must be used with https");
    }

    @Test
    void header() {
        var headers = new HeaderMap();
        headers.putAll(Headers.CONTENT_TYPE, List.of("contentType1", "contentType2"));
        when(exchange.getRequestHeaders()).thenReturn(headers);

        assertThat(request.header(HTTPHeaders.CONTENT_TYPE))
            .hasValue("contentType1");
    }
}
