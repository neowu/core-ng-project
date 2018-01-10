package core.framework.impl.web.service;

import core.framework.http.HTTPMethod;
import core.framework.util.ClasspathResources;
import core.framework.util.Maps;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class WebServiceClientBuilderTest {
    private TestWebService client;
    private WebServiceClientBuilder<TestWebService> builder;
    private WebServiceClient webServiceClient;

    @BeforeEach
    void createTestWebServiceClient() {
        webServiceClient = Mockito.mock(WebServiceClient.class);
        builder = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient);
        client = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("webservice-test/test-webservice-client.java"), sourceCode);
    }

    @Test
    void get() {
        TestWebService.TestResponse expectedResponse = new TestWebService.TestResponse();

        when(webServiceClient.serviceURL(startsWith("/test/:id"), eq(Maps.newHashMap("id", 1))))
                .thenReturn("http://localhost/test/1");
        when(webServiceClient.execute(HTTPMethod.GET, "http://localhost/test/1", null, null, Types.optional(TestWebService.TestResponse.class)))
                .thenReturn(Optional.of(expectedResponse));

        TestWebService.TestResponse response = client.get(1).get();
        assertSame(expectedResponse, response);
    }

    @Test
    void create() {
        when(webServiceClient.serviceURL(startsWith("/test/:id"), eq(Maps.newHashMap("id", 1))))
                .thenReturn("http://localhost/test/1");

        TestWebService.TestRequest request = new TestWebService.TestRequest();
        client.create(1, request);

        verify(webServiceClient).execute(HTTPMethod.PUT, "http://localhost/test/1", TestWebService.TestRequest.class, request, void.class);
    }

    @Test
    void patch() {
        when(webServiceClient.serviceURL(startsWith("/test/:id"), eq(Maps.newHashMap("id", 1))))
                .thenReturn("http://localhost/test/1");

        TestWebService.TestRequest request = new TestWebService.TestRequest();
        client.patch(1, request);

        verify(webServiceClient).execute(HTTPMethod.PATCH, "http://localhost/test/1", TestWebService.TestRequest.class, request, void.class);
    }
}
