package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class ControllerActionBuilderTest {
    @Test
    public void action() {
        assertEquals("web/get-root", new ControllerActionBuilder(HTTPMethod.GET, "/").build());
        assertEquals("web/get-path", new ControllerActionBuilder(HTTPMethod.GET, "/path").build());
        assertEquals("web/get-path", new ControllerActionBuilder(HTTPMethod.GET, "/path/").build());
        assertEquals("web/get-path-param", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param").build());
        assertEquals("web/get-path-param", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param(*)").build());
        assertEquals("web/get-path-param-param1", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param(*)/param1").build());
    }
}
