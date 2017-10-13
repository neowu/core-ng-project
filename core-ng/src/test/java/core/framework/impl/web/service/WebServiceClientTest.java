package core.framework.impl.web.service;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.util.Maps;
import core.framework.validate.ValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
public class WebServiceClientTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private WebServiceClient webServiceClient;
    private HTTPRequest request;

    @Before
    public void prepare() {
        webServiceClient = new WebServiceClient("http://localhost", null, new RequestBeanMapper(), null);
        request = Mockito.mock(HTTPRequest.class);
    }

    @Test
    public void addQueryParams() {
        Map<String, String> params = Maps.newLinkedHashMap();
        params.put("p1", "v1");
        params.put("p2", null);
        params.put("p3", "v3");

        webServiceClient.addQueryParams(request, params);

        verify(request, times(1)).addParam("p1", "v1");
        verify(request, never()).addParam("p2", null);
        verify(request, times(1)).addParam("p3", "v3");
    }

    @Test
    public void serviceURL() {
        assertEquals("http://localhost", webServiceClient.serviceURL("/", Maps.newHashMap()));     // as http standard, url without ending '/' will result in requestedPath = '/' on server side
        assertEquals("http://localhost/test", webServiceClient.serviceURL("/test", Maps.newHashMap()));
        assertEquals("http://localhost/test/", webServiceClient.serviceURL("/test/", Maps.newHashMap()));

        Map<String, Object> pathParams = Maps.newHashMap("id", "1+2");
        assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id(\\d+)", pathParams));
        assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id", pathParams));
    }

    @Test
    public void serviceURLWithEmptyPathParam() {
        exception.expect(ValidationException.class);
        exception.expectMessage("name=id");

        webServiceClient.serviceURL("/test/:id", Maps.newHashMap("id", ""));
    }

    @Test
    public void addRequestBeanWithGet() {
        TestWebService.TestSearchRequest requestBean = new TestWebService.TestSearchRequest();
        requestBean.intField = 23;
        webServiceClient.addRequestBean(request, HTTPMethod.GET, TestWebService.TestSearchRequest.class, requestBean);

        verify(request).addParam("int_field", "23");
    }

    @Test
    public void addRequestBeanWithPost() {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";
        webServiceClient.addRequestBean(request, HTTPMethod.POST, TestWebService.TestRequest.class, requestBean);

        verify(request).body(JSONMapper.toJSON(requestBean), ContentType.APPLICATION_JSON);
    }
}
