package core.framework.http;

import core.framework.util.Charsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
class ContentTypeTest {
    @Test
    void parse() {
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
    void value() {
        assertEquals("application/json; charset=utf-8", ContentType.APPLICATION_JSON.toString());
        assertEquals("application/octet-stream", ContentType.APPLICATION_OCTET_STREAM.toString());
    }

    @Test
    void ignoreUnsupportedCharset() {
        ContentType type = ContentType.parse("image/jpeg; charset=binary");

        assertEquals("image/jpeg", type.mediaType());
        assertFalse(type.charset().isPresent());
    }
}
