package core.framework.impl.web.service;

import core.framework.api.http.HTTPRequest;
import core.framework.api.util.Maps;
import core.framework.api.validate.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Map;

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
        webServiceClient = new WebServiceClient("http://localhost", null, null, null);
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
        Assert.assertEquals("http://localhost", webServiceClient.serviceURL("/", Maps.newHashMap()));     // as http standard, url without ending '/' will result in requestedPath = '/' on server side
        Assert.assertEquals("http://localhost/test", webServiceClient.serviceURL("/test", Maps.newHashMap()));
        Assert.assertEquals("http://localhost/test/", webServiceClient.serviceURL("/test/", Maps.newHashMap()));

        Map<String, Object> pathParams = Maps.newHashMap("id", "1+2");
        Assert.assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id(\\d+)", pathParams));
        Assert.assertEquals("http://localhost/test/1%2B2", webServiceClient.serviceURL("/test/:id", pathParams));
    }

    @Test
    public void serviceURLWithEmptyPathParam() {
        exception.expect(ValidationException.class);
        exception.expectMessage("name=id");

        webServiceClient.serviceURL("/test/:id", Maps.newHashMap("id", ""));
    }
}