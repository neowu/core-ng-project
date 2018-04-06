package core.framework.impl.web.response;

import core.framework.http.ContentType;
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
}
