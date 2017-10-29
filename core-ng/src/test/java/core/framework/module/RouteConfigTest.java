package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class RouteConfigTest {
    private RouteConfig config;

    @BeforeEach
    void createRouteConfig() {
        ModuleContext context = new ModuleContext(new BeanFactory(), new TestMockFactory());
        config = new RouteConfig(context);
    }

    @Test
    void route() {
        TestControllers controllers = new TestControllers();
        config.get("/route-test", controllers::get);
        config.post("/route-test", controllers::post);
        TestController controller = new TestController();
        config.put("/route-test", controller);
        config.delete("/route-test", controller);
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
        public Response execute(Request request) throws Exception {
            return null;
        }
    }
}
