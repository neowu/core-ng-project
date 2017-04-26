package core.framework.impl.web.route;

import core.framework.impl.web.request.PathParams;
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
    public void dynamicPathPattern() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/:var1");
        URLHandler handler2 = root.register("/path1/:var1");
        URLHandler handler3 = root.register("/path1/:var1/path2");
        URLHandler handler4 = root.register("/path1/:var1/:var2");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find("/value", pathParams);
        Assert.assertSame(handler1, found);
        Assert.assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value", pathParams);
        Assert.assertSame(handler2, found);
        Assert.assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value/path2", pathParams);
        Assert.assertSame(handler3, found);
        Assert.assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value1/value2", pathParams);
        Assert.assertSame(handler4, found);
        Assert.assertEquals("value1", pathParams.get("var1"));
        Assert.assertEquals("value2", pathParams.get("var2"));
    }

    @Test
    public void dynamicPathPatternNotMatchTrailingSlash() {
        PathNode root = new PathNode();
        root.register("/path1/:var1");

        PathParams pathParams = new PathParams();
        URLHandler foundHandler = root.find("/path1/", pathParams);
        Assert.assertNull(foundHandler);
    }

    @Test
    public void dynamicPathPatternsWithTrailingSlash() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/path1/:var");
        URLHandler handler2 = root.register("/path1/:var/");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find(Path.parse("/path1/value"), pathParams);
        Assert.assertSame(handler1, found);
        Assert.assertEquals("value", pathParams.get("var"));

        pathParams = new PathParams();
        found = root.find(Path.parse("/path1/value/"), pathParams);
        Assert.assertSame(handler2, found);
        Assert.assertEquals("value", pathParams.get("var"));
    }

    @Test
    public void wildcardPathPattern() {
        PathNode root = new PathNode();

        root.register("/:var1");
        root.register("/path1/path2/path3/path4/:var1");
        URLHandler handler = root.register("/path1/path2/:url(*)");

        PathParams pathParams = new PathParams();
        URLHandler matchedHandler = root.find("/path1/path2/path3/value", pathParams);
        Assert.assertSame(handler, matchedHandler);
        Assert.assertEquals("path3/value", pathParams.get("url"));

        pathParams = new PathParams();
        matchedHandler = root.find("/path1/path2/path3/value/", pathParams);
        Assert.assertSame(handler, matchedHandler);
        Assert.assertEquals("path3/value/", pathParams.get("url"));
    }

    @Test
    public void conflictDynamicPathPattern() {
        exception.expect(Error.class);
        exception.expectMessage("var1");
        exception.expectMessage("var2");

        PathNode root = new PathNode();

        root.register("/path1/:var1/path2");
        root.register("/path1/:var2/path3");
    }

    @Test
    public void conflictWildcardPathPattern() {
        exception.expect(Error.class);
        exception.expectMessage("var1");
        exception.expectMessage("var2");

        PathNode root = new PathNode();
        root.register("/path/:var1(*)");
        root.register("/path/:var2(*)");
    }
}