package core.framework.http;

import core.framework.api.http.HTTPStatus;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPResponseTest {
    @Test
    void text() {
        var response = new HTTPResponse(HTTPStatus.OK, Map.of(HTTPHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString()), Strings.bytes("value"));

        assertThat(response.contentType.toString()).isEqualTo(ContentType.TEXT_PLAIN.toString());
        assertThat(response.text()).isEqualTo("value");
    }
}
