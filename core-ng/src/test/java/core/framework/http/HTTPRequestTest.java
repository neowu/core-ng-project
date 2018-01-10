package core.framework.http;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class HTTPRequestTest {
    @Test
    void body() {
        HTTPRequest request = HTTPRequest.post("http://localhost/uri");
        request.body("text", ContentType.TEXT_PLAIN);

        assertEquals(HTTPMethod.POST, request.method());
        assertEquals(ContentType.TEXT_PLAIN, request.contentType());
        assertArrayEquals(Strings.bytes("text"), request.body());
    }

    @Test
    void accept() {
        HTTPRequest request = HTTPRequest.patch("http://localhost/uri");
        request.accept(ContentType.APPLICATION_JSON);

        assertEquals(ContentType.APPLICATION_JSON.toString(), request.headers().get(HTTPHeaders.ACCEPT));
    }

    @Test
    void method() {
        assertEquals(HTTPMethod.GET, HTTPRequest.get("http://localhost/uri").method());
        assertEquals(HTTPMethod.POST, HTTPRequest.post("http://localhost/uri").method());
        assertEquals(HTTPMethod.PUT, HTTPRequest.put("http://localhost/uri").method());
        assertEquals(HTTPMethod.DELETE, HTTPRequest.delete("http://localhost/uri").method());
        assertEquals(HTTPMethod.PATCH, HTTPRequest.patch("http://localhost/uri").method());
    }
}
