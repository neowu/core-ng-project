package core.framework.api.http;

import core.framework.api.util.Charsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author neo
 */
public class ContentTypeTest {
    @Test
    public void parse() {
        ContentType type = ContentType.parse("application/json; charset=utf-8");
        assertEquals("application/json", type.mediaType());
        assertEquals(Charsets.UTF_8, type.charset().get());

        type = ContentType.parse("image/png");
        assertEquals("image/png", type.mediaType());
        assertFalse(type.charset().isPresent());

        type = ContentType.parse("multipart/form-data; boundary=----WebKitFormBoundaryaANA7UQAvnwa2EkM");
        assertEquals("multipart/form-data", type.mediaType());
        assertFalse(type.charset().isPresent());
    }

    @Test
    public void value() {
        assertEquals("application/json; charset=utf-8", ContentType.APPLICATION_JSON.toString());
        assertEquals("application/octet-stream", ContentType.APPLICATION_OCTET_STREAM.toString());
    }

    @Test
    public void ignoreUnsupportedCharset() {
        ContentType type = ContentType.parse("image/jpeg; charset=binary");

        assertEquals("image/jpeg", type.mediaType());
        assertFalse(type.charset().isPresent());
    }
}