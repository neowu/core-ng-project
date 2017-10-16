package core.framework.impl.web;

import core.framework.http.HTTPMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ControllerActionBuilderTest {
    @Test
    void action() {
        assertEquals("web/get-root", new ControllerActionBuilder(HTTPMethod.GET, "/").build());
        assertEquals("web/get-path", new ControllerActionBuilder(HTTPMethod.GET, "/path").build());
        assertEquals("web/get-path", new ControllerActionBuilder(HTTPMethod.GET, "/path/").build());
        assertEquals("web/get-path-param", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param").build());
        assertEquals("web/get-path-param", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param(*)").build());
        assertEquals("web/get-path-param-param1", new ControllerActionBuilder(HTTPMethod.GET, "/path/:param(*)/param1").build());
    }
}
