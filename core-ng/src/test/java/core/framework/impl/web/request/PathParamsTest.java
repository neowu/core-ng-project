package core.framework.impl.web.request;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals("decode utf-8", "✓", pathParams.decodePathSegment("%E2%9C%93"));
        assertEquals("a b", pathParams.decodePathSegment("a%20b"));
        assertEquals("a+b", pathParams.decodePathSegment("a+b"));
        assertEquals("a=b", pathParams.decodePathSegment("a=b"));
        assertEquals("a?b", pathParams.decodePathSegment("a%3Fb"));
        assertEquals("a/b", pathParams.decodePathSegment("a%2Fb"));
        assertEquals("a&b", pathParams.decodePathSegment("a&b"));
    }
}