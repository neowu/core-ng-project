package core.framework.module;

import core.framework.api.json.Property;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.reflect.Classes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class APIConfigTest {
    private APIConfig config;
    private ModuleContext context;

    @BeforeEach
    void createAPIConfig() {
        context = new ModuleContext();
        config = new APIConfig();
        config.initialize(context, null);
    }

    @Test
    void service() {
        config.service(TestWebService.class, new TestWebServiceImpl());

        assertThat(config.serviceInterfaces).containsEntry(Classes.className(TestWebService.class), TestWebService.class);
    }

    @Test
    void bean() {
        config.bean(TestBean.class);

        assertThat(config.beanClasses).containsOnly(TestBean.class);

        assertThatThrownBy(() -> config.bean(TestBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is already registered");
    }

    @Test
    void client() {
        config.httpClient().timeout(Duration.ofSeconds(5));
        config.client(TestWebService.class, "http://localhost");

        TestWebService client = (TestWebService) context.beanFactory.bean(TestWebService.class, null);
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

    public static class TestBean {
        @Property(name = "value")
        public String value;
    }
}
