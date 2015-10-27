package core.framework.impl.web.site;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CDNManagerTest {
    CDNManager function;

    @Before
    public void createCDNFunction() {
        function = new CDNManager();
        function.hosts = new String[]{"host1", "host2"};
        function.version = "100";
    }

    @Test
    public void url() {
        String url = function.url("/image/image2.png");
        Assert.assertEquals("//host2/image/image2.png?v=100", url);

        url = function.url("/image/image3.png");
        Assert.assertEquals("//host1/image/image3.png?v=100", url);

        url = function.url("/image/image3.png?param=value");
        Assert.assertEquals("//host1/image/image3.png?param=value&v=100", url);
    }

    @Test
    public void absoluteURL() {
        Assert.assertEquals("//host2/image/image1.png", function.url("//host2/image/image1.png"));
    }
}