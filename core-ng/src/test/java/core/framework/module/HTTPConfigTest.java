package core.framework.module;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static core.framework.http.HTTPMethod.DELETE;
import static core.framework.http.HTTPMethod.GET;
import static core.framework.http.HTTPMethod.PATCH;
import static core.framework.http.HTTPMethod.POST;
import static core.framework.http.HTTPMethod.PUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HTTPConfigTest {
    private HTTPConfig config;

    @BeforeAll
    void createRouteConfig() {
        config = new HTTPConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void route() {
        var controllers = new TestControllers();
        config.route(GET, "/route-test", controllers::get);
        config.route(POST, "/route-test", controllers::post);

        var controller = new TestController();
        config.route(PUT, "/route-test", controller);
        config.route(DELETE, "/route-test", controller);
        config.route(PATCH, "/route-test", controller);
    }


    @Test
    void bean() {
        config.bean(TestBean.class);
        assertThat(config.context.apiController.beanClasses).contains(TestBean.class);

        config.bean(TestQueryParamBean.class);
        assertThat(config.context.apiController.beanClasses).contains(TestQueryParamBean.class);

        config.bean(TestEnum.class);
        assertThat(config.context.apiController.beanClasses).contains(TestEnum.class);

        assertThatThrownBy(() -> config.bean(TestBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is already registered");

        assertThatThrownBy(() -> config.bean(TestQueryParamBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is already registered");

        assertThatThrownBy(() -> config.bean(TestEnum.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is already registered");
    }

    @Test
    void routeWithReservedPath() {
        assertThatThrownBy(() -> config.route(GET, HTTPIOHandler.HEALTH_CHECK_PATH, new TestController()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("/health-check is reserved path");
    }

    @Test
    void intercept() {
        assertThatThrownBy(() -> config.intercept(invocation -> null))
                .isInstanceOf(Error.class)
                .hasMessageContaining("interceptor class must not be anonymous class or lambda");
    }

    public enum TestEnum {
        @Property(name = "value")
        VALUE
    }

    static class TestControllers {
        public Response get(Request request) {
            return null;
        }

        public Response post(Request request) {
            return null;
        }
    }

    static class TestController implements Controller {
        @Override
        public Response execute(Request request) {
            return null;
        }
    }

    public static class TestBean {
        @Property(name = "value")
        public String value;
    }

    public static class TestQueryParamBean {
        @QueryParam(name = "value")
        public String value;
    }
}
