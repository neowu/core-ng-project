package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.HealthCheckHandler;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RouteConfigTest {
    private RouteConfig config;

    @BeforeEach
    void createRouteConfig() {
        config = new RouteConfig(new ModuleContext());
    }

    @Test
    void route() {
        TestControllers controllers = new TestControllers();
        config.get("/route-test", controllers::get);
        config.post("/route-test", controllers::post);
        TestController controller = new TestController();
        config.put("/route-test", controller);
        config.delete("/route-test", controller);
        config.patch("/route-test", controller);
    }

    @Test
    void routeWithReservedPath() {
        assertThatThrownBy(() -> config.add(HTTPMethod.GET, HealthCheckHandler.PATH, new TestController()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("/health-check is reserved path");
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
}
