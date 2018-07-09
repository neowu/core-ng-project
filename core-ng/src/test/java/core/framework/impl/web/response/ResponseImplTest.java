package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.web.Response;
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ResponseImplTest {
    @Test
    void contentType() {
        ResponseImpl response = new ResponseImpl(null);
        response.contentType(ContentType.APPLICATION_JSON);

        assertThat(response.headers).containsEntry(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
    }

    @Test
    void useHeaderToUpdateContentType() {
        ResponseImpl response = new ResponseImpl(null);
        assertThatThrownBy(() -> response.header("Content-Type", "application/json"))
                .hasMessageContaining("must not use header()");
    }

    @Test
    void bean() {
        assertThatThrownBy(() -> Response.bean(null))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean must not be null");
    }

    @Test
    void redirect() {
        assertThatThrownBy(() -> Response.redirect(null, HTTPStatus.OK))
                .isInstanceOf(Error.class)
                .hasMessageContaining("invalid redirect status");

        assertThat(Response.redirect("/path").status()).isEqualTo(HTTPStatus.SEE_OTHER);
    }
}
