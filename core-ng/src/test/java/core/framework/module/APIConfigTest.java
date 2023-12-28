package core.framework.module;

import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.http.HTTPClient;
import core.framework.internal.module.ModuleContext;
import core.framework.web.service.WebServiceClientInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class APIConfigTest {
    private APIConfig config;

    @BeforeEach
    void createAPIConfig() {
        config = new APIConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void service() {
        config.service(TestWebService.class, new TestWebServiceImpl());

        assertThat(config.context.apiController.serviceInterfaces).contains(TestWebService.class);
    }

    @Test
    void client() {
        config.client(TestWebService.class, "http://localhost", mock(HTTPClient.class));

        TestWebService client = (TestWebService) config.context.beanFactory.bean(TestWebService.class, null);
        assertThat(client).isNotNull();
    }

    @Test
    void clientWithCustomHTTPClint() {
        HTTPClient httpClient = HTTPClient.builder().build();
        config.client(TestWebService.class, "http://localhost", httpClient);

        TestWebService client = (TestWebService) config.context.beanFactory.bean(TestWebService.class, null);
        assertThat(client).isNotNull();
    }

    public interface TestWebService {
        @PUT
        @Path("/test/:id")
        void put(@PathParam("id") Integer id);
    }

    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public void put(Integer id) {
        }
    }

    public static class TestWebServiceClientInterceptor implements WebServiceClientInterceptor {

    }
}
