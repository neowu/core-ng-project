package core.framework.internal.web.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class PathTest {
    @Test
    void parseRootURL() {
        Path path = Path.parse("/");
        assertThat(path.value).isEqualTo("/");
        assertThat(path.next).isNull();
    }

    @Test
    void parseOneLevelURL() {
        Path path = Path.parse("/path1");
        assertEquals("/", path.value);
        assertEquals("path1", path.next.value);
        assertNull(path.next.next);
    }

    @Test
    void parseOneLevelURLWithTrailingSlash() {
        Path path = Path.parse("/path1/");
        assertEquals("/", path.value);
        assertEquals("path1", path.next.value);
        assertEquals("/", path.next.next.value);
        assertNull(path.next.next.next);
    }

    @Test
    void parseURL() {
        Path path = Path.parse("/path1/path2");
        assertEquals("/", path.value);
        assertEquals("path1", path.next.value);
        assertEquals("path2", path.next.next.value);
        assertNull(path.next.next.next);
    }

    @Test
    void parseURLWithTrailingSlash() {
        Path path = Path.parse("/path1/path2/");
        assertEquals("/", path.value);
        assertEquals("path1", path.next.value);
        assertEquals("path2", path.next.next.value);
        assertEquals("/", path.next.next.next.value);
        assertNull(path.next.next.next.next);
    }

    @Test
    void subPath() {
        Path path = Path.parse("/path1/path2/");
        assertEquals("/path1/path2/", path.subPath());
        assertEquals("path1/path2/", path.next.subPath());
        assertEquals("path2/", path.next.next.subPath());
        assertEquals("/", path.next.next.next.subPath());
    }
}
