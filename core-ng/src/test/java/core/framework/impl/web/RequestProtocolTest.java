package core.framework.impl.web;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class RequestProtocolTest {
    @Test
    public void withXForwardedPort() {
        RequestProtocol protocol = new RequestProtocol(null, null, 80, "443");
        Assert.assertEquals(443, protocol.port());
    }

    @Test
    public void withMultipleXForwardedPort() {
        RequestProtocol protocol = new RequestProtocol(null, null, 80, "443, 80");
        Assert.assertEquals(443, protocol.port());
    }

    @Test
    public void withoutXForwardedPort() {
        RequestProtocol protocol = new RequestProtocol(null, null, 80, null);
        Assert.assertEquals(80, protocol.port());
    }
}