package core.framework.impl.web.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class PathParamsTest {
    PathParams pathParams;

    @Before
    public void createPathParams() {
        pathParams = new PathParams();
    }

    @Test
    public void decodePathSegment() {
        Assert.assertEquals("v1", pathParams.decodePathSegment("v1"));
        Assert.assertEquals("v1 v2", pathParams.decodePathSegment("v1%20v2"));
        Assert.assertEquals("v1+v2", pathParams.decodePathSegment("v1+v2"));
        Assert.assertEquals("utf-8:✓", pathParams.decodePathSegment("utf-8:%E2%9C%93"));
    }
}