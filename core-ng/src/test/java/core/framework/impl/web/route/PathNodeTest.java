package core.framework.impl.web.route;

import core.framework.impl.web.PathParams;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class PathNodeTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void rootPath() {
        PathNode root = new PathNode();

        URLHandler rootHandler = root.register("/");
        URLHandler found = root.find("/", new PathParams());

        Assert.assertNotNull(found);
        Assert.assertSame(rootHandler, found);
    }

    @Test
    public void dynamicPathPatterns() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/:var1");
        URLHandler handler2 = root.register("/path1/:var1");
        URLHandler handler3 = root.register("/path1/:var1/path2");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find("/value", pathParams);
        Assert.assertSame(handler1, found);
        Assert.assertEquals("value", pathParams.get("var1", String.class));

        pathParams = new PathParams();
        found = root.find("/path1/value", pathParams);
        Assert.assertSame(handler2, found);
        Assert.assertEquals("value", pathParams.get("var1", String.class));

        pathParams = new PathParams();
        found = root.find("/path1/value/path2", pathParams);
        Assert.assertSame(handler3, found);
        Assert.assertEquals("value", pathParams.get("var1", String.class));
    }

    @Test
    public void dynamicRegexPathPatterns() {
        PathNode root = new PathNode();

        root.register("/:var1");
        URLHandler handler2 = root.register("/path1/:var1(\\d+)/path2");
        URLHandler handler3 = root.register("/path1/:var1(\\D+)/path2");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find(Path.parse("/path1/100/path2"), pathParams);
        Assert.assertSame(handler2, found);
        Assert.assertEquals("100", pathParams.get("var1", String.class));

        pathParams = new PathParams();
        found = root.find(Path.parse("/path1/value/path2"), pathParams);
        Assert.assertSame(handler3, found);
        Assert.assertEquals("value", pathParams.get("var1", String.class));
    }

    @Test
    public void dynamicRegexPathPatternsWithTrailingSlash() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/path1/:var");
        URLHandler handler2 = root.register("/path1/:var/");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find(Path.parse("/path1/value"), pathParams);
        Assert.assertSame(handler1, found);
        Assert.assertEquals("value", pathParams.get("var", String.class));

        pathParams = new PathParams();
        found = root.find(Path.parse("/path1/value/"), pathParams);
        Assert.assertSame(handler2, found);
        Assert.assertEquals("value", pathParams.get("var", String.class));
    }

    @Test
    public void wildcardPathPattern() {
        PathNode root = new PathNode();

        root.register("/:var1");
        root.register("/path1/path2/path3/:var1(\\d+)");
        URLHandler handler = root.register("/path1/path2/:url(*)");

        PathParams pathParams = new PathParams();
        URLHandler matchedHandler = root.find("/path1/path2/path3/value", pathParams);
        Assert.assertSame(handler, matchedHandler);
        Assert.assertEquals("path3/value", pathParams.get("url", String.class));

        pathParams = new PathParams();
        matchedHandler = root.find("/path1/path2/path3/value/", pathParams);
        Assert.assertSame(handler, matchedHandler);
        Assert.assertEquals("path3/value/", pathParams.get("url", String.class));
    }

    @Test
    public void conflictDynamicRegexPathPatterns() {
        exception.expect(Error.class);
        exception.expectMessage("var1");
        exception.expectMessage("var2");

        PathNode root = new PathNode();

        root.register("/path1/:var1/path2");
        root.register("/path1/:var2/path3");
    }

    @Test
    public void conflictWildcardPathPatterns() {
        exception.expect(Error.class);
        exception.expectMessage("path1");
        exception.expectMessage("path2");

        PathNode root = new PathNode();

        root.register("/path/:path1(*)");
        root.register("/path/:path2(*)");
    }
}