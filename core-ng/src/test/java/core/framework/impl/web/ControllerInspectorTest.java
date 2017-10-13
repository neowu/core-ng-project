package core.framework.impl.web;

import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class ControllerInspectorTest {
    @Test
    public void methodReference() throws NoSuchMethodException {
        ControllerInspector inspector = new ControllerInspector(new TestControllers()::get);
        assertEquals(TestControllers.class, inspector.targetClass);
        assertEquals(TestControllers.class.getDeclaredMethod("get", Request.class), inspector.targetMethod);
        assertEquals(TestControllers.class.getCanonicalName() + ".get", inspector.controllerInfo);
    }

    @Test
    public void lambdaMethod() {
        ControllerInspector inspector = new ControllerInspector(request -> null);
        assertThat(inspector.targetClass.getCanonicalName(), startsWith(ControllerInspectorTest.class.getCanonicalName()));
        assertNotNull(inspector.targetMethod);
        assertThat(inspector.controllerInfo, startsWith(ControllerInspectorTest.class.getCanonicalName() + "."));
    }

    @Test
    public void staticClass() throws NoSuchMethodException {
        ControllerInspector inspector = new ControllerInspector(new TestController());
        assertEquals(TestController.class, inspector.targetClass);
        assertEquals(TestController.class.getMethod("execute", Request.class), inspector.targetMethod);
        assertEquals(TestController.class.getCanonicalName() + ".execute", inspector.controllerInfo);
    }

    public static class TestController implements Controller {
        @Override
        public Response execute(Request request) throws Exception {
            return null;
        }
    }

    public static class TestControllers {
        public Response get(Request request) {
            return null;
        }
    }
}
