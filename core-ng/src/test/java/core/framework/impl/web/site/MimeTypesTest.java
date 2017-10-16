package core.framework.impl.web.site;

import core.framework.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class MimeTypesTest {
    @Test
    void get() {
        assertNull(MimeTypes.get("file"));

        ContentType contentType = MimeTypes.get("favicon.ico");
        assertNotNull(contentType);
        assertEquals("image/x-icon", contentType.mediaType());
    }
}
