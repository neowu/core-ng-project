package core.framework.impl.web;

import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ControllerInspectorTest {
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

//    @Test
//    public void methodReference() throws NoSuchMethodException {
//        ControllerInspector inspector = new ControllerInspector(new TestControllers()::get);
//        Assert.assertEquals(TestControllers.class.getCanonicalName(), inspector.targetClassName);
//        Assert.assertEquals("get", inspector.targetMethodName);
//        Assert.assertEquals(TestControllers.class.getDeclaredMethod("get", Request.class), inspector.targetMethod);
//    }
//
//    @Test
//    public void anonymousMethod() {
//        ControllerInspector inspector = new ControllerInspector(request -> null);
//        Assert.assertEquals(ControllerInspectorTest.class.getCanonicalName(), inspector.targetClassName);
//        Assert.assertNotNull(inspector.targetMethodName);
//        Assert.assertNotNull(inspector.targetMethod);
//    }

    @Test
    public void staticClass() throws NoSuchMethodException {
        ControllerInspector inspector = new ControllerInspector(new TestController());
        Assert.assertEquals(TestController.class.getCanonicalName(), inspector.targetClassName);
        Assert.assertEquals("execute", inspector.targetMethodName);
        Assert.assertEquals(TestController.class.getMethod("execute", Request.class), inspector.targetMethod);
    }
}