package core.framework.impl.web.request;

import core.framework.api.web.exception.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class PathParamsTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @Test
    public void putEmptyPathParam() {
        exception.expect(BadRequestException.class);
        exception.expectMessage("name=id, value=");

        pathParams.put("id", "");
    }
}