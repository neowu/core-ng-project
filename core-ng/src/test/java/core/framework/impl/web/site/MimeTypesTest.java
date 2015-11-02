package core.framework.impl.web.site;

import core.framework.api.http.ContentType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class MimeTypesTest {
    @Test
    public void get() {
        Assert.assertNull(MimeTypes.get("file"));

        ContentType contentType = MimeTypes.get("favicon.ico");
        Assert.assertNotNull(contentType);
        Assert.assertEquals("image/x-icon", contentType.mediaType());
    }
}