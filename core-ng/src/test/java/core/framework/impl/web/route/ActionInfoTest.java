package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ActionInfoTest {
    @Test
    public void action() {
        Assert.assertEquals("web/get-root", new ActionInfo(HTTPMethod.GET, "/").action());
        Assert.assertEquals("web/get-path", new ActionInfo(HTTPMethod.GET, "/path").action());
        Assert.assertEquals("web/get-path", new ActionInfo(HTTPMethod.GET, "/path/").action());
        Assert.assertEquals("web/get-path-param", new ActionInfo(HTTPMethod.GET, "/path/:param").action());
        Assert.assertEquals("web/get-path-param", new ActionInfo(HTTPMethod.GET, "/path/:param(\\d+)").action());
        Assert.assertEquals("web/get-path-param", new ActionInfo(HTTPMethod.GET, "/path/:param(\\d+)").action());
        Assert.assertEquals("web/get-path-param-param1", new ActionInfo(HTTPMethod.GET, "/path/:param(\\d+)/param1").action());
    }
}