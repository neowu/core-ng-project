package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class EncodingsTest {
    @Test
    public void hex() {
        assertEquals("74657374206d657373616765", Encodings.hex(Strings.bytes("test message")));
    }

    @Test
    public void decodeHex() {
        assertEquals("test message", new String(Encodings.decodeHex("74657374206d657373616765"), Charsets.UTF_8));
    }

    @Test
    public void base64() {
        assertEquals("", Encodings.base64(""));
        // from http://en.wikipedia.org/wiki/Base64
        assertEquals("bGVhc3VyZS4=", Encodings.base64("leasure."));
    }

    @Test
    public void decodeBase64() {
        // from http://en.wikipedia.org/wiki/Base64
        assertEquals("leasure.", new String(Encodings.decodeBase64("bGVhc3VyZS4="), Charsets.UTF_8));
    }

    @Test
    public void base64URLSafe() {
        String message = "leasure.";
        String encodedMessage = Encodings.base64URLSafe(Strings.bytes(message));
        assertEquals(message, new String(Encodings.decodeBase64(encodedMessage), Charsets.UTF_8));
    }

    @Test
    public void url() {
        String urlParamValue = "key=value?";
        // from http://en.wikipedia.org/wiki/Percent-encoding
        assertEquals("key%3Dvalue%3F", Encodings.url(urlParamValue));
    }

    @Test
    public void decodeURL() {
        assertEquals("key=value?", Encodings.decodeURL("key%3Dvalue%3F"));
    }

    @Test
    public void urlPath() {
        Assert.assertEquals("v1", Encodings.urlPath("v1"));
        Assert.assertEquals("the path should use %20 for space, where queryString uses + for space", "v1%20v2", Encodings.urlPath("v1 v2"));
    }
}