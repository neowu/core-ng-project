package core.framework.internal.web.service;

import core.framework.http.HTTPMethod;
import core.framework.internal.asm.CodeBuilder;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;
import core.framework.web.service.WebServiceClientProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class WebServiceClientBuilderTest {
    @Mock
    WebServiceClient webServiceClient;
    private TestWebService client;
    private WebServiceClientBuilder<TestWebService> builder;

    @BeforeEach
    void createTestWebServiceClient() {
        builder = new WebServiceClientBuilder<>(TestWebService.class, webServiceClient);
        client = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualTo(ClasspathResources.text("webservice-test/test-webservice-client.java"));
    }

    @Test
    void intercept() {
        assertThat(client).isInstanceOf(WebServiceClientProxy.class);
        var interceptor = new TestWebServiceClientInterceptor();
        ((WebServiceClientProxy) client).intercept(interceptor);
        verify(webServiceClient).intercept(interceptor);
    }

    @Test
    void get() {
        doCallRealMethod().when(webServiceClient).logCallWebService(anyString());
        var expectedResponse = new TestWebService.TestResponse();
        when(webServiceClient.execute(HTTPMethod.GET, "/test/1", null, null, Types.optional(TestWebService.TestResponse.class)))
                .thenReturn(Optional.of(expectedResponse));

        TestWebService.TestResponse response = client.get(1).orElseThrow();
        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void create() {
        doCallRealMethod().when(webServiceClient).logCallWebService(anyString());
        var request = new TestWebService.TestRequest();
        client.create(1, request);

        verify(webServiceClient).execute(HTTPMethod.PUT, "/test/1", TestWebService.TestRequest.class, request, void.class);
    }

    @Test
    void patch() {
        doCallRealMethod().when(webServiceClient).logCallWebService(anyString());
        var request = new TestWebService.TestRequest();
        client.patch(1, request);

        verify(webServiceClient).execute(HTTPMethod.PATCH, "/test/1", TestWebService.TestRequest.class, request, void.class);
    }

    @Test
    void buildPath() {
        var builder = new CodeBuilder();
        this.builder.buildPath(builder, "/test", Map.of());
        assertThat(builder.build()).isEqualToIgnoringWhitespace("String path = \"/test\";");

        builder = new CodeBuilder();
        this.builder.buildPath(builder, "/test/:id", Map.of("id", 0));
        assertThat(builder.build()).contains("builder.append(\"/test/\").append(core.framework.internal.web.service.PathParamHelper.toString(param0));");

        builder = new CodeBuilder();
        this.builder.buildPath(builder, "/:id/status", Map.of("id", 0));
        assertThat(builder.build())
                .contains("builder.append(\"/\").append(core.framework.internal.web.service.PathParamHelper.toString(param0));")
                .contains("builder.append(\"/status\");");

        builder = new CodeBuilder();
        this.builder.buildPath(builder, "/test/:key1/:key2", Map.of("key1", 0, "key2", 1));
        assertThat(builder.build())
                .contains("builder.append(\"/test/\").append(core.framework.internal.web.service.PathParamHelper.toString(param0));")
                .contains("builder.append(\"/\").append(core.framework.internal.web.service.PathParamHelper.toString(param1));");

        builder = new CodeBuilder();
        this.builder.buildPath(builder, "/test/:key1/:key2/", Map.of("key1", 0, "key2", 1));
        assertThat(builder.build())
                .contains("builder.append(\"/test/\").append(core.framework.internal.web.service.PathParamHelper.toString(param0));")
                .contains("builder.append(\"/\").append(core.framework.internal.web.service.PathParamHelper.toString(param1));")
                .contains("builder.append(\"/\");");
    }

}
