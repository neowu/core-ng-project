package core.framework.impl.template;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CDNManagerTest {
    private CDNManager manager;

    @Before
    public void createCDNManager() {
        manager = new CDNManager();
        manager.host("cdn");
    }

    @Test
    public void url() {
        String url = manager.url("/image/image2.png");
        Assert.assertEquals("//cdn/image/image2.png", url);

        url = manager.url("/image/image3.png");
        Assert.assertEquals("//cdn/image/image3.png", url);

        url = manager.url("/image/image3.png?param=value");
        Assert.assertEquals("//cdn/image/image3.png?param=value", url);
    }

    @Test
    public void absoluteURL() {
        Assert.assertEquals("//host2/image/image1.png", manager.url("//host2/image/image1.png"));
    }
}