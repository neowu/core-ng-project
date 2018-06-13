package core.framework.http;

import core.framework.util.Charsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ContentTypeTest {
    @Test
    void parse() {
        ContentType type = ContentType.parse("application/json; charset=utf-8");
        assertThat(type.mediaType()).isEqualTo("application/json");
        assertThat(type.charset()).get().isEqualTo(Charsets.UTF_8);

        type = ContentType.parse("image/png");
        assertThat(type.mediaType()).isEqualTo("image/png");
        assertThat(type.charset()).isNotPresent();

        type = ContentType.parse("multipart/form-data; boundary=----WebKitFormBoundaryaANA7UQAvnwa2EkM");
        assertThat(type.mediaType()).isEqualTo("multipart/form-data");
        assertThat(type.charset()).isNotPresent();
    }

    @Test
    void value() {
        assertThat(ContentType.APPLICATION_JSON.toString()).isEqualTo("application/json; charset=utf-8");
        assertThat(ContentType.APPLICATION_OCTET_STREAM.toString()).isEqualTo("application/octet-stream");
    }

    @Test
    void ignoreUnsupportedCharset() {
        ContentType type = ContentType.parse("image/jpeg; charset=binary");

        assertThat(type.mediaType()).isEqualTo("image/jpeg");
        assertThat(type.charset()).isNotPresent();
    }
}
