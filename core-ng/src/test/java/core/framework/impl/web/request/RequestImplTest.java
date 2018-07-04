package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.impl.validate.ValidationException;
import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.TestBean;
import core.framework.impl.web.bean.TestQueryParamBean;
import core.framework.util.Maps;
import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * @author neo
 */
class RequestImplTest {
    private RequestImpl request;
    private RequestBeanMapper mapper;

    @BeforeEach
    void createRequest() {
        mapper = spy(new RequestBeanMapper(new BeanClassNameValidator()));
        request = new RequestImpl(null, mapper);
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
        doThrow(new ValidationException(Maps.newHashMap())).when(mapper).fromJSON(eq(TestBean.class), any());
        request.method = HTTPMethod.POST;
        request.contentType = ContentType.APPLICATION_JSON;
        request.body = Strings.bytes("{\"big_decimal_field\": 1}");
        assertThatThrownBy(() -> request.bean(TestBean.class))
                .isInstanceOf(BadRequestException.class)
                .satisfies(exception -> assertThat(((BadRequestException) exception).errorCode()).isEqualTo("VALIDATION_ERROR"));
    }
}
