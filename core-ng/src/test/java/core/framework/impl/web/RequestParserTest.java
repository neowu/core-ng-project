package core.framework.impl.web;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RequestParserTest {
    RequestParser parser = new RequestParser();

    @Test
    public void clientIP() {
        assertEquals("127.0.0.1", parser.clientIP("127.0.0.1", null));
        assertEquals("127.0.0.1", parser.clientIP("127.0.0.1", ""));
        assertEquals("108.0.0.1", parser.clientIP("127.0.0.1", "108.0.0.1"));
        assertEquals("108.0.0.1", parser.clientIP("127.0.0.1", "108.0.0.1, 10.10.10.10"));
    }

    @Test
    public void port() {
        Assert.assertEquals(80, parser.port(80, null));
        Assert.assertEquals(443, parser.port(80, "443"));
        Assert.assertEquals(443, parser.port(80, "443, 80"));
    }
}