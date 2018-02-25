package core.framework.http;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * @author neo
 */
class HTTPRequestTest {
    @Test
    void body() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, "http://localhost/uri");
        request.body("text", ContentType.TEXT_PLAIN);

        assertThat(request.method()).isEqualTo(HTTPMethod.POST);
        assertThat(request.contentType()).isEqualTo(ContentType.TEXT_PLAIN);
        assertThat(request.body()).containsExactly(Strings.bytes("text"));
    }

    @Test
    void accept() {
        HTTPRequest request = new HTTPRequest(HTTPMethod.PATCH, "http://localhost/uri");
        request.accept(ContentType.APPLICATION_JSON);

        assertThat(request.headers().get(HTTPHeaders.ACCEPT)).isEqualTo(ContentType.APPLICATION_JSON.toString());
    }

    @Test
    void method() {
        assertThat(new HTTPRequest(HTTPMethod.GET, "http://localhost/uri").method()).isEqualTo(HTTPMethod.GET);
        assertThat(new HTTPRequest(HTTPMethod.POST, "http://localhost/uri").method()).isEqualTo(HTTPMethod.POST);
    }

    @Test
    void basicAuth() {  // refer to https://en.wikipedia.org/wiki/Basic_access_authentication
        HTTPRequest request = new HTTPRequest(HTTPMethod.GET, "http://localhost/uri");
        request.basicAuth("Aladdin", "OpenSesame");

        assertThat(request.headers()).contains(entry(HTTPHeaders.AUTHORIZATION, "Basic QWxhZGRpbjpPcGVuU2VzYW1l"));
    }
}
