package core.framework.impl.web.route;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author neo
 */
public class PathTest {
    @Test
    public void parseRootURL() {
        Path path = Path.parse("/");
        Assert.assertEquals("/", path.value);
        Assert.assertNull(path.next);
    }

    @Test
    public void parseOneLevelURL() {
        Path path = Path.parse("/path1");
        Assert.assertEquals("/", path.value);
        Assert.assertEquals("path1", path.next.value);
        Assert.assertNull(path.next.next);
    }

    @Test
    public void parseOneLevelURLWithTrailingSlash() {
        Path path = Path.parse("/path1/");
        Assert.assertEquals("/", path.value);
        Assert.assertEquals("path1", path.next.value);
        Assert.assertEquals("/", path.next.next.value);
        Assert.assertNull(path.next.next.next);
    }

    @Test
    public void parseURL() {
        Path path = Path.parse("/path1/path2");
        Assert.assertEquals("/", path.value);
        Assert.assertEquals("path1", path.next.value);
        Assert.assertEquals("path2", path.next.next.value);
        Assert.assertNull(path.next.next.next);
    }

    @Test
    public void parseURLWithTrailingSlash() {
        Path path = Path.parse("/path1/path2/");
        Assert.assertEquals("/", path.value);
        Assert.assertEquals("path1", path.next.value);
        Assert.assertEquals("path2", path.next.next.value);
        Assert.assertEquals("/", path.next.next.next.value);
        Assert.assertNull(path.next.next.next.next);
    }

    @Test
    public void subPath() {
        Path path = Path.parse("/path1/path2/");
        Assert.assertEquals("/path1/path2/", path.subPath());
        Assert.assertEquals("path1/path2/", path.next.subPath());
        Assert.assertEquals("path2/", path.next.next.subPath());
        Assert.assertEquals("/", path.next.next.next.subPath());
    }
}