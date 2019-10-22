package core.framework.internal.web.site;

import core.framework.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MimeTypesTest {
    @Test
    void get() {
        assertThat(MimeTypes.get("file")).isNull();

        ContentType contentType = MimeTypes.get("favicon.ico");
        assertThat(contentType).isNotNull();
        assertThat(contentType.mediaType).isEqualTo("image/x-icon");
    }
}
