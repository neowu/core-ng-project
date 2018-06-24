package core.framework.module;

import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.reflect.Classes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIConfigTest {
    private APIConfig config;

    @BeforeEach
    void createAPIConfig() {
        config = new APIConfig();
        config.initialize(new ModuleContext(), null);
    }

    @Test
    void service() {
        config.service(TestWebService.class, new TestWebServiceImpl());

        assertThat(config.serviceInterfaces).containsEntry(Classes.className(TestWebService.class), TestWebService.class);
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
}
