package core.framework.impl.web;

import core.framework.http.ContentType;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void proceed() {
        HeaderMap headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(Headers.CONTENT_LENGTH, 50);
        assertTrue(predicate.resolve(headers));
    }

    @Test
    void proceedWithChunk() {  // chunk does not have content-length header
        HeaderMap headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.TEXT_CSS.toString());
        assertTrue(predicate.resolve(headers));
    }

    @Test
    void skipIfContentTypeNotMatch() {
        HeaderMap headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, "image/png");
        assertFalse(predicate.resolve(headers));
    }

    @Test
    void skipIfContentLengthIsTooSmall() {
        HeaderMap headers = new HeaderMap();
        headers.put(Headers.CONTENT_TYPE, ContentType.TEXT_PLAIN.toString());
        headers.put(Headers.CONTENT_LENGTH, 10);
        assertFalse(predicate.resolve(headers));
    }
}
