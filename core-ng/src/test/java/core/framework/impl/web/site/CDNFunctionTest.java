package core.framework.impl.web.site;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CDNFunctionTest {
    CDNFunction function;

    @Before
    public void createCDNFunction() {
        function = new CDNFunction();
        function.hosts = new String[]{"host1", "host2"};
    }

    @Test
    public void buildURL() {
        String url = function.buildURL("/image/image2.png");
        Assert.assertEquals("//host2/image/image2.png", url);

        url = function.buildURL("/image/image3.png");
        Assert.assertEquals("//host1/image/image3.png", url);
    }
}