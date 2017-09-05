package core.framework.impl.web.service;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
public class WebServiceClientBuilderTest {
    private TestWebService client;
    private WebServiceClientBuilder<TestWebService> builder;
    private WebServiceClient webServiceClient;

    @Before
    public void createTestWebServiceClient() {
        webServiceClient = Mockito.mock(WebServiceClient.class);
        builder = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient);
        client = builder.build();
    }

    @Test
    public void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-client-test/test-webservice-client.java").replaceAll("\r\n", "\n"), sourceCode);
    }

    @Test
    public void get() {
        TestWebService.TestResponse expectedResponse = new TestWebService.TestResponse();

        when(webServiceClient.serviceURL(startsWith("/test/:id"), eq(Maps.newHashMap("id", 1))))
                .thenReturn("http://localhost/test/1");
        when(webServiceClient.execute(HTTPMethod.GET, "http://localhost/test/1", null, null, TestWebService.TestResponse.class))
                .thenReturn(expectedResponse);

        TestWebService.TestResponse response = client.get(1);
        assertSame(expectedResponse, response);
    }

    @Test
    public void create() {
        when(webServiceClient.serviceURL(startsWith("/test/:id"), eq(Maps.newHashMap("id", 1))))
                .thenReturn("http://localhost/test/1");

        TestWebService.TestRequest request = new TestWebService.TestRequest();
        client.create(1, request);

        verify(webServiceClient).execute(HTTPMethod.PUT, "http://localhost/test/1", TestWebService.TestRequest.class, request, void.class);
    }
}
