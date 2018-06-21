package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.json.JSON;
import core.framework.log.Severity;
import core.framework.util.Maps;
import core.framework.util.Strings;
import core.framework.validate.ValidationException;
import core.framework.web.service.RemoteServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class WebServiceClientTest {
    private WebServiceClient webServiceClient;

    @BeforeEach
    void createWebServiceClient() {
        webServiceClient = new WebServiceClient("http://localhost", null, new RequestBeanMapper(new BeanClassNameValidator()), null);
    }

    @Test
    void addQueryParams() {
        var request = new HTTPRequest(HTTPMethod.POST, "/");

        Map<String, String> params = Maps.newLinkedHashMap();
        params.put("p1", "v1");
        params.put("p2", null);
        params.put("p3", "v3");

        webServiceClient.addQueryParams(request, params);

        assertThat(request.params()).containsExactly(entry("p1", "v1"), entry("p3", "v3"));
    }

    @Test
    void serviceURL() {
        assertThat(webServiceClient.serviceURL("/", Maps.newHashMap())).isEqualTo("http://localhost");     // as http standard, url without ending '/' will result in requestedPath = '/' on server side
        assertThat(webServiceClient.serviceURL("/test", Maps.newHashMap())).isEqualTo("http://localhost/test");
        assertThat(webServiceClient.serviceURL("/test/", Maps.newHashMap())).isEqualTo("http://localhost/test/");

        Map<String, Object> pathParams = Maps.newHashMap("id", "1+2");
        assertThat(webServiceClient.serviceURL("/test/:id(\\d+)", pathParams)).isEqualTo("http://localhost/test/1%2B2");
        assertThat(webServiceClient.serviceURL("/test/:id", pathParams)).isEqualTo("http://localhost/test/1%2B2");
    }

    @Test
    void serviceURLWithEmptyPathParam() {
        assertThatThrownBy(() -> webServiceClient.serviceURL("/test/:id", Maps.newHashMap("id", "")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name=id");
    }

    @Test
    void addRequestBeanWithGet() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "/");

        TestWebService.TestSearchRequest requestBean = new TestWebService.TestSearchRequest();
        requestBean.intField = 23;
        webServiceClient.addRequestBean(request, HTTPMethod.GET, TestWebService.TestSearchRequest.class, requestBean);

        assertThat(request.params()).containsExactly(entry("int_field", "23"));
    }

    @Test
    void addRequestBeanWithPost() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "/");

        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "123value";
        webServiceClient.addRequestBean(request, HTTPMethod.POST, TestWebService.TestRequest.class, requestBean);

        assertThat(request.body()).isEqualTo(JSONMapper.toJSON(requestBean));
        assertThat(request.contentType()).isEqualTo(ContentType.APPLICATION_JSON);
    }

    @Test
    void validateResponse() {
        webServiceClient.validateResponse(new HTTPResponse(HTTPStatus.OK, Maps.newHashMap(), null));
    }

    @Test
    void validateResponseWithErrorResponse() {
        ErrorResponse response = new ErrorResponse();
        response.severity = "WARN";
        response.errorCode = "NOT_FOUND";
        response.message = "not found";

        assertThatThrownBy(() -> webServiceClient.validateResponse(new HTTPResponse(HTTPStatus.NOT_FOUND, Maps.newHashMap(), Strings.bytes(JSON.toJSON(response)))))
                .isInstanceOf(RemoteServiceException.class)
                .satisfies(throwable -> {
                    RemoteServiceException exception = (RemoteServiceException) throwable;
                    assertThat(exception.severity()).isEqualTo(Severity.WARN);
                    assertThat(exception.errorCode()).isEqualTo(response.errorCode);
                    assertThat(exception.getMessage()).isEqualTo(response.message);
                    assertThat(exception.status).isEqualTo(HTTPStatus.NOT_FOUND);
                });
    }
}
