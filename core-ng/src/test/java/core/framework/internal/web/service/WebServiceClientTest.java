package core.framework.internal.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.web.bean.BeanMappers;
import core.framework.internal.web.bean.RequestBeanMapper;
import core.framework.internal.web.bean.ResponseBeanMapper;
import core.framework.json.JSON;
import core.framework.log.Severity;
import core.framework.util.Strings;
import core.framework.web.service.RemoteServiceException;
import core.framework.web.service.WebServiceClientInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class WebServiceClientTest {
    private WebServiceClient webServiceClient;
    private HTTPClient httpClient;

    @BeforeEach
    void createWebServiceClient() {
        httpClient = mock(HTTPClient.class);

        var registry = new BeanMappers();
        var beanClassNameValidator = new BeanClassNameValidator();
        var requestBeanMapper = new RequestBeanMapper(registry);
        var responseBeanMapper = new ResponseBeanMapper(registry);
        requestBeanMapper.registerQueryParamBean(TestWebService.TestSearchRequest.class, beanClassNameValidator);
        requestBeanMapper.registerRequestBean(TestWebService.TestRequest.class, beanClassNameValidator);
        responseBeanMapper.register(ErrorResponse.class, beanClassNameValidator);

        webServiceClient = new WebServiceClient("http://localhost", httpClient, requestBeanMapper, responseBeanMapper);
    }

    @Test
    void putRequestBeanWithGet() {
        var request = new HTTPRequest(HTTPMethod.GET, "/");

        var requestBean = new TestWebService.TestSearchRequest();
        requestBean.intField = 23;
        webServiceClient.putRequestBean(request, HTTPMethod.GET, TestWebService.TestSearchRequest.class, requestBean);

        assertThat(request.params).contains(entry("int_field", "23"));
    }

    @Test
    void putRequestBeanWithPost() {
        var request = new HTTPRequest(HTTPMethod.POST, "/");

        var requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "123value";
        webServiceClient.putRequestBean(request, HTTPMethod.POST, TestWebService.TestRequest.class, requestBean);

        assertThat(new String(request.body, UTF_8)).isEqualTo(JSON.toJSON(requestBean));
        assertThat(request.contentType).isEqualTo(ContentType.APPLICATION_JSON);
    }

    @Test
    void validateResponse() {
        webServiceClient.validateResponse(new HTTPResponse(200, Map.of(), null));
    }

    @Test
    void validateResponseWithErrorResponse() {
        var response = new ErrorResponse();
        response.severity = "WARN";
        response.errorCode = "NOT_FOUND";
        response.message = "not found";

        assertThatThrownBy(() -> webServiceClient.validateResponse(new HTTPResponse(404, Map.of(), Strings.bytes(JSON.toJSON(response)))))
            .isInstanceOf(RemoteServiceException.class)
            .satisfies(throwable -> {
                RemoteServiceException exception = (RemoteServiceException) throwable;
                assertThat(exception.severity()).isEqualTo(Severity.WARN);
                assertThat(exception.errorCode()).isEqualTo(response.errorCode);
                assertThat(exception.getMessage()).isEqualTo(response.message);
                assertThat(exception.status).isEqualTo(HTTPStatus.NOT_FOUND);
            });
    }

    @Test
    void validateResponseWithEmptyBody() {
        assertThatThrownBy(() -> webServiceClient.validateResponse(new HTTPResponse(503, Map.of(), new byte[0])))
            .isInstanceOf(RemoteServiceException.class)
            .satisfies(throwable -> {
                RemoteServiceException exception = (RemoteServiceException) throwable;
                assertThat(exception.severity()).isEqualTo(Severity.ERROR);
                assertThat(exception.errorCode()).isEqualTo("REMOTE_SERVICE_ERROR");
                assertThat(exception.getMessage()).isEqualTo("failed to call remote service, statusCode=503");
                assertThat(exception.status).isEqualTo(HTTPStatus.SERVICE_UNAVAILABLE);
            });
    }

    @Test
    void parseHTTPStatus() {
        assertThat(WebServiceClient.parseHTTPStatus(200)).isEqualTo(HTTPStatus.OK);
    }

    @Test
    void parseUnsupportedHTTPStatus() {
        assertThatThrownBy(() -> WebServiceClient.parseHTTPStatus(525))
            .isInstanceOf(Error.class);
    }

    @Test
    void intercept() {
        WebServiceClientInterceptor interceptor = mock(WebServiceClientInterceptor.class);
        webServiceClient.intercept(interceptor);

        HTTPResponse response = new HTTPResponse(200, Map.of(), new byte[0]);
        when(httpClient.execute(any())).thenReturn(response);

        webServiceClient.execute(HTTPMethod.GET, "/api", null, null, void.class);

        verify(interceptor).onRequest(argThat(request -> HTTPMethod.GET.equals(request.method)
            && request.requestURI().equals("http://localhost/api")));
        verify(interceptor).onResponse(response);
    }
}
