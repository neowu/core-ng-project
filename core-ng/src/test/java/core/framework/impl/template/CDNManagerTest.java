package core.framework.impl.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class CDNManagerTest {
    private CDNManager manager;

    @BeforeEach
    void createCDNManager() {
        manager = new CDNManager();
        manager.host("cdn");
    }

    @Test
    void url() {
        String url = manager.url("/image/image2.png");
        assertEquals("//cdn/image/image2.png", url);

        url = manager.url("/image/image3.png");
        assertEquals("//cdn/image/image3.png", url);

        url = manager.url("/image/image3.png?param=value");
        assertEquals("//cdn/image/image3.png?param=value", url);
    }

    @Test
    void absoluteURL() {
        assertEquals("//host2/image/image1.png", manager.url("//host2/image/image1.png"));
    }
}
