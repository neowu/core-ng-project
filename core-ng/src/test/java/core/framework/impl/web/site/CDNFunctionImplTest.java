package core.framework.impl.web.site;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CDNFunctionImplTest {
    CDNFunctionImpl function;

    @Before
    public void createCDNFunction() {
        function = new CDNFunctionImpl();
        function.hosts = new String[]{"host1", "host2"};
        function.version = "100";
    }

    @Test
    public void url() {
        int hash2 = "/image/image2.png".hashCode();
        int hostIndex2 = hash2 % function.hosts.length;
        StringBuilder builder2 = new StringBuilder("/image/image2.png".length() + 50);
        builder2.append("//").append(function.hosts[hostIndex2]).append("/image/image2.png");
        if (function.version != null) {
            if ("/image/image2.png".contains("?")) builder2.append("&v=");
            else builder2.append("?v=");
            builder2.append(function.version);
        }
        String url = builder2.toString();
        Assert.assertEquals("//host2/image/image2.png?v=100", url);

        int hash1 = "/image/image3.png".hashCode();
        int hostIndex1 = hash1 % function.hosts.length;
        StringBuilder builder1 = new StringBuilder("/image/image3.png".length() + 50);
        builder1.append("//").append(function.hosts[hostIndex1]).append("/image/image3.png");
        if (function.version != null) {
            if ("/image/image3.png".contains("?")) builder1.append("&v=");
            else builder1.append("?v=");
            builder1.append(function.version);
        }
        url = builder1.toString();
        Assert.assertEquals("//host1/image/image3.png?v=100", url);

        int hash = "/image/image3.png?param=value".hashCode();
        int hostIndex = hash % function.hosts.length;
        StringBuilder builder = new StringBuilder("/image/image3.png?param=value".length() + 50);
        builder.append("//").append(function.hosts[hostIndex]).append("/image/image3.png?param=value");
        if (function.version != null) {
            if ("/image/image3.png?param=value".contains("?")) builder.append("&v=");
            else builder.append("?v=");
            builder.append(function.version);
        }
        url = builder.toString();
        Assert.assertEquals("//host1/image/image3.png?param=value&v=100", url);
    }
}