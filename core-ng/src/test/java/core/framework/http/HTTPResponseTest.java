package core.framework.http;

import core.framework.api.http.HTTPStatus;
import core.framework.util.Maps;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class HTTPResponseTest {
    @Test
    void text() {
        HTTPResponse response = new HTTPResponse(HTTPStatus.OK, Maps.newHashMap(HTTPHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString()), Strings.bytes("value"));

        assertEquals(ContentType.TEXT_PLAIN.toString(), response.contentType().orElse(null).toString());
        assertEquals("value", response.text());
    }
}
