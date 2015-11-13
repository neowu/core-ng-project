package core.framework.api.http;

import core.framework.api.util.Charsets;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class ContentTypeTest {
    @Test
    public void parse() {
        ContentType type = ContentType.parse("application/json; charset=utf-8");
        Assert.assertEquals("application/json", type.mediaType());
        Assert.assertEquals(Charsets.UTF_8, type.charset().get());

        type = ContentType.parse("image/png");
        Assert.assertEquals("image/png", type.mediaType());
        Assert.assertFalse(type.charset().isPresent());

        type = ContentType.parse("multipart/form-data; boundary=----WebKitFormBoundaryaANA7UQAvnwa2EkM");
        Assert.assertEquals("multipart/form-data", type.mediaType());
        Assert.assertFalse(type.charset().isPresent());
    }

    @Test
    public void value() {
        Assert.assertEquals("application/json; charset=utf-8", ContentType.APPLICATION_JSON.toString());
        Assert.assertEquals("application/octet-stream", ContentType.APPLICATION_OCTET_STREAM.toString());
    }
}