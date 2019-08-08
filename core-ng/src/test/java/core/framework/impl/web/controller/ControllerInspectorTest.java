package core.framework.impl.web.controller;

import core.framework.module.LambdaController;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ControllerInspectorTest {
    @Test
    void methodReference() throws NoSuchMethodException {
        var inspector = new ControllerInspector((LambdaController) new TestControllers()::get);
        assertThat(inspector.targetClass).isEqualTo(TestControllers.class);
        assertThat(inspector.targetMethod).isEqualTo(TestControllers.class.getDeclaredMethod("get", Request.class));
        assertThat(inspector.controllerInfo).isEqualTo(TestControllers.class.getCanonicalName() + ".get");
    }

    @Test
    void lambdaMethod() {
        var inspector = new ControllerInspector((LambdaController) request -> null);
        assertThat(inspector.targetClass.getCanonicalName()).startsWith(ControllerInspectorTest.class.getCanonicalName());
        assertThat(inspector.targetMethod).isNotNull();
        assertThat(inspector.controllerInfo).startsWith(ControllerInspectorTest.class.getCanonicalName() + ".");
    }

    @Test
    void staticClass() throws NoSuchMethodException {
        var inspector = new ControllerInspector(new TestController());
        assertThat(inspector.targetClass).isEqualTo(TestController.class);
        assertThat(inspector.targetMethod).isEqualTo(TestController.class.getMethod("execute", Request.class));
        assertThat(inspector.controllerInfo).isEqualTo(TestController.class.getCanonicalName() + ".execute");
    }

    public static class TestController implements Controller {
        @Override
        public Response execute(Request request) {
            return null;
        }
    }

    public static class TestControllers {
        public Response get(Request request) {
            return null;
        }
    }
}
