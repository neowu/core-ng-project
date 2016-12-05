package core.framework.impl.web.request;

import core.framework.api.web.exception.MethodNotAllowedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RequestParserTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RequestParser parser;

    @Before
    public void createRequestParser() {
        parser = new RequestParser();
    }

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

    @Test
    public void httpMethod() {
        exception.expect(MethodNotAllowedException.class);
        exception.expectMessage("method=TRACK");

        parser.httpMethod("TRACK");
    }
}