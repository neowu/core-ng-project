package core.framework.impl.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RemoteAddressTest {
    @Test
    public void toStringWithXForwardedFor() {
        RemoteAddress address = new RemoteAddress("127.0.0.1", "108.35.7.101, 10.0.0.70");
        assertEquals("108.35.7.101, 10.0.0.70, 127.0.0.1", address.toString());
    }

    @Test
    public void toStringWithoutXForwardedFor() {
        RemoteAddress address = new RemoteAddress("10.9.10.19", null);
        assertEquals("10.9.10.19", address.toString());
    }

    @Test
    public void getClientIPWithoutXForwardedFor() {
        RemoteAddress address = new RemoteAddress("127.0.0.1", null);
        assertEquals("127.0.0.1", address.clientIP());
    }

    @Test
    public void getClientIPWithSingleXForwardedFor() {
        RemoteAddress address = new RemoteAddress("127.0.0.1", "108.0.0.1");
        assertEquals("108.0.0.1", address.clientIP());
    }

    @Test
    public void getClientIPWithMultipleXForwardedFor() {
        RemoteAddress address = new RemoteAddress("127.0.0.1", "108.0.0.1, 10.10.10.10");
        assertEquals("108.0.0.1", address.clientIP());
    }

    @Test
    public void getClientIPWithEmptyXForwardedFor() {
        RemoteAddress address = new RemoteAddress("127.0.0.1", "");
        assertEquals("127.0.0.1", address.clientIP());
    }
}