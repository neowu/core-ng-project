package core.framework.impl.web.service;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
public class WebServiceClientBuilderTest {
    @Test
    public void get() {
        WebServiceClient webServiceClient = Mockito.mock(WebServiceClient.class);
        Map<String, String> pathParams = Maps.newHashMap();
        pathParams.put("id", "1");
        when(webServiceClient.execute(eq(HTTPMethod.GET), anyString(), eq(pathParams), isNull(Type.class), isNull(), eq(TestWebService.TestResponse.class)))
            .thenReturn(new TestWebService.TestResponse());

        TestWebService client = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient).build();

        TestWebService.TestResponse response = client.get(1);
        Assert.assertNotNull(response);
    }

    @Test
    public void create() {
        WebServiceClient webServiceClient = Mockito.mock(WebServiceClient.class);
        Map<String, String> pathParams = Maps.newHashMap();
        pathParams.put("id", "1");
        when(webServiceClient.execute(eq(HTTPMethod.POST), anyString(), eq(pathParams), eq(TestWebService.TestRequest.class), isNull(), eq(Void.class)))
            .thenReturn(null);

        TestWebService client = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient).build();

        client.create(1, new TestWebService.TestRequest());
    }
}