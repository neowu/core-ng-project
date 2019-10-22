package core.framework.internal.web.route;

import core.framework.internal.web.request.PathParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathNodeTest {
    private PathNode root;

    @BeforeEach
    void createPathNode() {
        root = new PathNode(null);
    }

    @Test
    void rootPath() {
        URLHandler rootHandler = root.register("/");
        URLHandler found = root.find("/", new PathParams());

        assertThat(found).isNotNull().isSameAs(rootHandler);
    }

    @Test
    void dynamicPathPattern() {
        URLHandler handler1 = root.register("/:var1");
        URLHandler handler2 = root.register("/path1/:var1");
        URLHandler handler3 = root.register("/path1/:var1/path2");
        URLHandler handler4 = root.register("/path1/:var1/:var2");

        var pathParams = new PathParams();
        URLHandler found = root.find("/value", pathParams);
        assertThat(found).isSameAs(handler1);
        assertThat(pathParams.get("var1")).isEqualTo("value");

        pathParams = new PathParams();
        found = root.find("/path1/value", pathParams);
        assertThat(found).isSameAs(handler2);
        assertThat(pathParams.get("var1")).isEqualTo("value");

        pathParams = new PathParams();
        found = root.find("/path1/value/path2", pathParams);
        assertThat(found).isSameAs(handler3);
        assertThat(pathParams.get("var1")).isEqualTo("value");

        pathParams = new PathParams();
        found = root.find("/path1/value1/value2", pathParams);
        assertThat(found).isSameAs(handler4);
        assertThat(pathParams.get("var1")).isEqualTo("value1");
        assertThat(pathParams.get("var2")).isEqualTo("value2");
    }

    @Test
    void dynamicPathPatternNotMatchTrailingSlash() {
        root.register("/path1/:var1");

        assertThat(root.find("/path1/", new PathParams())).isNull();
    }

    @Test
    void dynamicPathPatternsWithTrailingSlash() {
        URLHandler handler1 = root.register("/path1/:var");
        URLHandler handler2 = root.register("/path1/:var/");

        var pathParams = new PathParams();
        URLHandler found = root.find("/path1/value", pathParams);
        assertThat(found).isSameAs(handler1);
        assertThat(pathParams.get("var")).isEqualTo("value");

        pathParams = new PathParams();
        found = root.find("/path1/value/", pathParams);
        assertThat(found).isSameAs(handler2);
        assertThat(pathParams.get("var")).isEqualTo("value");
    }

    @Test
    void wildcardPathPattern() {
        root.register("/:var1");
        root.register("/path1/path2/path3/path4/:var1");
        URLHandler handler = root.register("/path1/path2/:url(*)");

        var pathParams = new PathParams();
        URLHandler found = root.find("/path1/path2/path3/value", pathParams);
        assertThat(found).isSameAs(handler);
        assertThat(pathParams.get("url")).isEqualTo("path3/value");

        pathParams = new PathParams();
        found = root.find("/path1/path2/path3/value/", pathParams);
        assertThat(found).isSameAs(handler);
        assertThat(pathParams.get("url")).isEqualTo("path3/value/");
    }

    @Test
    void conflictDynamicPathPattern() {
        root.register("/path1/:var1/path2");
        assertThatThrownBy(() -> root.register("/path1/:var2/path3"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("param=var2, conflictedParam=var1");
    }

    @Test
    void conflictWildcardPathPattern() {
        root.register("/path/:var1(*)");

        assertThatThrownBy(() -> root.register("/path/:var2(*)"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("param=var2, conflictedParam=var1");
    }

    @Test
    void invalidWildcardVariable() {
        assertThatThrownBy(() -> root.register("/path/:var1(*)/"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("wildcard path variable must be the last");
    }
}
