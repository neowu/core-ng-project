package core.framework.impl.web.route;

import core.framework.impl.web.request.PathParams;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class PathNodeTest {
    @Test
    void rootPath() {
        PathNode root = new PathNode();

        URLHandler rootHandler = root.register("/");
        URLHandler found = root.find("/", new PathParams());

        assertNotNull(found);
        assertSame(rootHandler, found);
    }

    @Test
    void dynamicPathPattern() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/:var1");
        URLHandler handler2 = root.register("/path1/:var1");
        URLHandler handler3 = root.register("/path1/:var1/path2");
        URLHandler handler4 = root.register("/path1/:var1/:var2");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find("/value", pathParams);
        assertSame(handler1, found);
        assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value", pathParams);
        assertSame(handler2, found);
        assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value/path2", pathParams);
        assertSame(handler3, found);
        assertEquals("value", pathParams.get("var1"));

        pathParams = new PathParams();
        found = root.find("/path1/value1/value2", pathParams);
        assertSame(handler4, found);
        assertEquals("value1", pathParams.get("var1"));
        assertEquals("value2", pathParams.get("var2"));
    }

    @Test
    void dynamicPathPatternNotMatchTrailingSlash() {
        PathNode root = new PathNode();
        root.register("/path1/:var1");

        PathParams pathParams = new PathParams();
        URLHandler foundHandler = root.find("/path1/", pathParams);
        assertNull(foundHandler);
    }

    @Test
    void dynamicPathPatternsWithTrailingSlash() {
        PathNode root = new PathNode();

        URLHandler handler1 = root.register("/path1/:var");
        URLHandler handler2 = root.register("/path1/:var/");

        PathParams pathParams = new PathParams();
        URLHandler found = root.find(Path.parse("/path1/value"), pathParams);
        assertSame(handler1, found);
        assertEquals("value", pathParams.get("var"));

        pathParams = new PathParams();
        found = root.find(Path.parse("/path1/value/"), pathParams);
        assertSame(handler2, found);
        assertEquals("value", pathParams.get("var"));
    }

    @Test
    void wildcardPathPattern() {
        PathNode root = new PathNode();

        root.register("/:var1");
        root.register("/path1/path2/path3/path4/:var1");
        URLHandler handler = root.register("/path1/path2/:url(*)");

        PathParams pathParams = new PathParams();
        URLHandler matchedHandler = root.find("/path1/path2/path3/value", pathParams);
        assertSame(handler, matchedHandler);
        assertEquals("path3/value", pathParams.get("url"));

        pathParams = new PathParams();
        matchedHandler = root.find("/path1/path2/path3/value/", pathParams);
        assertSame(handler, matchedHandler);
        assertEquals("path3/value/", pathParams.get("url"));
    }

    @Test
    void conflictDynamicPathPattern() {
        PathNode root = new PathNode();

        root.register("/path1/:var1/path2");
        Error error = assertThrows(Error.class, () -> root.register("/path1/:var2/path3"));
        assertThat(error.getMessage()).contains("var1").contains("var2");
    }

    @Test
    void conflictWildcardPathPattern() {
        PathNode root = new PathNode();
        root.register("/path/:var1(*)");

        Error error = assertThrows(Error.class, () -> root.register("/path/:var2(*)"));
        assertThat(error.getMessage()).contains("var1").contains("var2");
    }
}
