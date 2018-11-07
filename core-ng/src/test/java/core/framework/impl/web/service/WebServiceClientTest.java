package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.web.bean.BeanMapperRegistry;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.json.JSON;
import core.framework.log.Severity;
import core.framework.util.Strings;
import core.framework.web.service.RemoteServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
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
        var registry = new BeanMapperRegistry();
        webServiceClient = new WebServiceClient("http://localhost", null, new RequestBeanMapper(registry), new ResponseBeanMapper(registry));
    }

    @Test
    void putRequestBeanWithGet() {
        var request = new HTTPRequest(HTTPMethod.POST, "/");

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
        webServiceClient.validateResponse(new HTTPResponse(HTTPStatus.OK, Map.of(), null));
    }

    @Test
    void validateResponseWithErrorResponse() {
        var response = new ErrorResponse();
        response.severity = "WARN";
        response.errorCode = "NOT_FOUND";
        response.message = "not found";

        assertThatThrownBy(() -> webServiceClient.validateResponse(new HTTPResponse(HTTPStatus.NOT_FOUND, Map.of(), Strings.bytes(JSON.toJSON(response)))))
                .isInstanceOf(RemoteServiceException.class)
                .satisfies(throwable -> {
                    RemoteServiceException exception = (RemoteServiceException) throwable;
                    assertThat(exception.severity()).isEqualTo(Severity.WARN);
                    assertThat(exception.errorCode()).isEqualTo(response.errorCode);
                    assertThat(exception.getMessage()).isEqualTo("failed to call remote service, status=NOT_FOUND, error=not found");
                    assertThat(exception.status).isEqualTo(HTTPStatus.NOT_FOUND);
                });
    }
}
