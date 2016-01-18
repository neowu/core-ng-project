package core.framework.impl.web.request;

import core.framework.api.web.exception.BadRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    public void putEmptyPathParam() {
        exception.expect(BadRequestException.class);
        exception.expectMessage("name=id, value=");

        pathParams.put("id", "");
    }
}