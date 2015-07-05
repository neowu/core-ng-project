package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class EncodingsTest {
    @Test
    public void encodeHex() {
        assertEquals("74657374206d657373616765", Encodings.hex("test message".getBytes()));
    }

    @Test
    public void encodeBase64WithEmptyString() {
        assertEquals("", Encodings.base64(""));
    }

    @Test
    public void encodeBase64() {
        // from http://en.wikipedia.org/wiki/Base64
        assertEquals("bGVhc3VyZS4=", Encodings.base64("leasure."));
    }

    @Test
    public void decodeBase64() {
        // from http://en.wikipedia.org/wiki/Base64
        assertEquals("leasure.", new String(Encodings.decodeBase64("bGVhc3VyZS4=")));
    }

    @Test
    public void decodeBase64URLSafe() {
        String message = "leasure.";
        String encodedMessage = Encodings.base64URLSafe(message.getBytes());
        assertEquals(message, new String(Encodings.decodeBase64(encodedMessage)));
    }

    @Test
    public void encodeURL() {
        String urlParamValue = "key=value?";
        // from http://en.wikipedia.org/wiki/Percent-encoding
        assertEquals("key%3Dvalue%3F", Encodings.url(urlParamValue));
    }

    @Test
    public void decodeURL() {
        assertEquals("key=value?", Encodings.decodeURL("key%3Dvalue%3F"));
    }
}