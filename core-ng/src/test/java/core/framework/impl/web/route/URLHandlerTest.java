package core.framework.impl.web.route;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class URLHandlerTest {
    URLHandler handler;

    @Before
    public void createURLHandler() {
        handler = new URLHandler(null);
    }

    @Test
    public void actionInfo() {
        Assert.assertEquals("root", handler.actionInfo("/"));
        Assert.assertEquals("path", handler.actionInfo("/path"));
        Assert.assertEquals("path", handler.actionInfo("/path/"));
        Assert.assertEquals("path-param", handler.actionInfo("/path/:param"));
        Assert.assertEquals("path-param", handler.actionInfo("/path/:param(\\d+)"));
        Assert.assertEquals("path-param", handler.actionInfo("/path/:param(\\d+)"));
        Assert.assertEquals("path-param-param1", handler.actionInfo("/path/:param(\\d+)/param1"));
    }
}