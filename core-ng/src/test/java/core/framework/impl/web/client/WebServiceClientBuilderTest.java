package core.framework.impl.web.client;

import core.framework.api.http.HTTPMethod;
import core.framework.impl.web.service.TestWebService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
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
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "1");
        when(webServiceClient.execute(eq(HTTPMethod.GET), anyString(), eq(pathParams), isNull(), eq(TestWebService.TestResponse.class)))
            .thenReturn(new TestWebService.TestResponse());

        TestWebService client = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient).build();

        TestWebService.TestResponse response = client.get(1);
        Assert.assertNotNull(response);
    }
}