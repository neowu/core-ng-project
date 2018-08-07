package core.framework.impl.web;

import core.framework.http.ContentType;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class GZipPredicateTest {
    private GZipPredicate predicate;

    @BeforeEach
    void createGZipPredicateTest() {
        predicate = new GZipPredicate();
    }

    @Test
    void resolve() {
        var headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(Headers.CONTENT_LENGTH, 50);
        assertThat(predicate.resolve(headers)).isTrue();
    }

    @Test
    void resolveWithChunk() {  // chunk does not have content-length header
        var headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.TEXT_CSS.toString());
        assertThat(predicate.resolve(headers)).isTrue();
    }

    @Test
    void skipIfContentTypeNotMatch() {
        var headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, "image/png");
        assertThat(predicate.resolve(headers)).isFalse();
    }

    @Test
    void skipIfContentLengthIsTooSmall() {
        var headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString());
        headers.put(Headers.CONTENT_LENGTH, 10);
        assertThat(predicate.resolve(headers)).isFalse();
    }

    @Test
    void skipWithoutContentType() {
        var headers = new HeaderMap();
        assertThat(predicate.resolve(headers)).isFalse();
    }
}
