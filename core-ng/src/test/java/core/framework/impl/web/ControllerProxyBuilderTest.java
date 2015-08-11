package core.framework.impl.web;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ControllerProxyBuilderTest {
    @Test
    public void actionInfo() {
        ControllerProxyBuilder builder = new ControllerProxyBuilder(null, null, null);

        Assert.assertEquals("root", builder.actionInfo("/"));
        Assert.assertEquals("path", builder.actionInfo("/path"));
        Assert.assertEquals("path", builder.actionInfo("/path/"));
        Assert.assertEquals("path-param", builder.actionInfo("/path/:param"));
        Assert.assertEquals("path-param", builder.actionInfo("/path/:param(\\d+)"));
        Assert.assertEquals("path-param", builder.actionInfo("/path/:param(\\d+)"));
        Assert.assertEquals("path-param-param1", builder.actionInfo("/path/:param(\\d+)/param1"));
    }
}