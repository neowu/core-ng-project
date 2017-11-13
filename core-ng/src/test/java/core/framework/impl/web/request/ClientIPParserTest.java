package core.framework.impl.web.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ClientIPParserTest {
    private ClientIPParser parser;

    @BeforeEach
    void createClientIPParser() {
        parser = new ClientIPParser();
    }

    @Test
    void withoutProxy() {
        assertEquals("127.0.0.1", parser.parse("127.0.0.1", null));
        assertEquals("127.0.0.1", parser.parse("127.0.0.1", ""));
    }

    @Test
    void withProxy() {
        assertEquals("108.0.0.1", parser.parse("127.0.0.1", "108.0.0.1"));
        assertEquals("108.0.0.1", parser.parse("127.0.0.1", " 108.0.0.1 "));
        assertEquals("108.0.0.1", parser.parse("127.0.0.1", "108.0.0.1, 10.10.10.10"));
    }

    @Test
    void exceedMaxForwardedIPs() {
        assertEquals("108.0.0.1", parser.parse("10.0.0.1", "192.168.0.1,108.0.0.1,10.0.0.1"));
        assertEquals("108.0.0.1", parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.1, 108.0.0.1, 10.0.0.1"));

        parser.maxForwardedIPs = 1;
        assertEquals("10.0.0.1", parser.parse("10.0.0.1", "192.168.0.1,108.0.0.1,10.0.0.1"));
        assertEquals("10.0.0.1", parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.1, 108.0.0.1, 10.0.0.1"));
    }

    @Test
    void illegalXForwardedFor() {
        assertEquals("108.0.0.1", parser.parse("10.0.0.1", ",108.0.0.1,10.0.0.1"));
        assertEquals("108.0.0.1", parser.parse("10.0.0.1", "text , 108.0.0.1, 10.0.0.1"));
    }
}
