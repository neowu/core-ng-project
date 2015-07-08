package core.framework.impl.web.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class WebServiceClientTest {
    WebServiceClient client = new WebServiceClient(null, null, null);

    @Test
    public void encodePathParam() {
        Assert.assertEquals("v1", client.encodePathParam("v1"));

        Assert.assertEquals("the path should use %20 for space, where queryString uses + for space", "v1%20v2", client.encodePathParam("v1 v2"));
    }
}