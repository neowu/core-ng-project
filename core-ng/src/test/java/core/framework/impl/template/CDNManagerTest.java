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
        manager.hosts("host1", "host2");
        manager.version("100");
    }

    @Test
    public void url() {
        String url = manager.url("/image/image2.png");
        Assert.assertEquals("//host2/image/image2.png?v=100", url);

        url = manager.url("/image/image3.png");
        Assert.assertEquals("//host1/image/image3.png?v=100", url);

        url = manager.url("/image/image3.png?param=value");
        Assert.assertEquals("//host1/image/image3.png?param=value&v=100", url);
    }

    @Test
    public void absoluteURL() {
        Assert.assertEquals("//host2/image/image1.png", manager.url("//host2/image/image1.png"));
    }
}