package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.json.JSONMapper;
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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class WebServiceClientTest {
    private WebServiceClient webServiceClient;

    @BeforeEach
    void createWebServiceClient() {
        webServiceClient = new WebServiceClient("http://localhost", null, new RequestBeanMapper(), null);
    }

    @Test
    void addQueryParams() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "/");

        Map<String, String> params = Maps.newLinkedHashMap();
        params.put("p1", "v1");
        params.put("p2", null);
        params.put("p3", "v3");

        webServiceClient.addQueryParams(request, params);

        assertEquals(2, request.params().size());
        assertEquals("v1", request.params().get("p1"));
        assertEquals("v3", request.params().get("p3"));
    }

    @Test
    void serviceURL() {
        assertEquals("http://localhost", webServiceClient.serviceURL("/", Maps.newHashMap()));     // as http standard, url without ending '/' will result in requestedPath = '/' on server side
        assertEquals("http://localhost/test", webServiceClient.serviceURL("/test", Maps.newHashMap()));
        assertEquals("http://localhost/test/", webServiceClient.serviceURL("/test/", Maps.newHashMap()));

        Map<String, Object> pathParams = Maps.newHashMap("id", "1+2");
        assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id(\\d+)", pathParams));
        assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id", pathParams));
    }

    @Test
    void serviceURLWithEmptyPathParam() {
        ValidationException exception = assertThrows(ValidationException.class, () -> webServiceClient.serviceURL("/test/:id", Maps.newHashMap("id", "")));
        assertThat(exception.getMessage()).contains("name=id");
    }

    @Test
    void addRequestBeanWithGet() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "/");

        TestWebService.TestSearchRequest requestBean = new TestWebService.TestSearchRequest();
        requestBean.intField = 23;
        webServiceClient.addRequestBean(request, HTTPMethod.GET, TestWebService.TestSearchRequest.class, requestBean);

        assertEquals(1, request.params().size());
        assertEquals("23", request.params().get("int_field"));
    }

    @Test
    void addRequestBeanWithPost() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "/");

        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "123value";
        webServiceClient.addRequestBean(request, HTTPMethod.POST, TestWebService.TestRequest.class, requestBean);

        assertArrayEquals(JSONMapper.toJSON(requestBean), request.body());
        assertEquals(ContentType.APPLICATION_JSON, request.contentType());
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

        RemoteServiceException exception = assertThrows(RemoteServiceException.class, () -> webServiceClient.validateResponse(new HTTPResponse(HTTPStatus.NOT_FOUND, Maps.newHashMap(), Strings.bytes(JSON.toJSON(response)))));
        assertEquals(Severity.WARN, exception.severity());
        assertEquals(response.errorCode, exception.errorCode());
        assertEquals(response.message, exception.getMessage());
    }
}
