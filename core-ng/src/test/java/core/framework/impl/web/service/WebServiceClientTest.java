package core.framework.impl.web.service;

import core.framework.api.http.HTTPRequest;
import core.framework.api.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
public class WebServiceClientTest {
    WebServiceClient webServiceClient;
    HTTPRequest request;

    @Before
    public void prepare() {
        webServiceClient = new WebServiceClient(null, null, null, null);
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
}