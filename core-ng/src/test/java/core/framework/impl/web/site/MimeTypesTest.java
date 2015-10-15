package core.framework.impl.web.site;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class MimeTypesTest {
    @Test
    public void get() {
        Assert.assertNull(MimeTypes.get("file"));
        Assert.assertEquals("image/x-icon", MimeTypes.get("favicon.ico"));
    }
}