package core.framework.internal.web.sys;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.cache.CacheImpl;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class CacheControllerTest {
    private CacheController controller;
    private Map<String, CacheImpl<?>> caches;

    @BeforeEach
    void createCacheController() {
        caches = new HashMap<>();
        controller = new CacheController(caches);
    }

    @Test
    void cache() {
        assertThatThrownBy(() -> controller.cache("notExistingCache"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void get() {
        CacheImpl<?> cache = mock(CacheImpl.class);
        when(cache.get("key")).thenReturn(Optional.empty());
        caches.put("cache1", cache);

        var request = mock(Request.class);
        when(request.clientIP()).thenReturn("127.0.0.1");
        when(request.pathParam("name")).thenReturn("cache1");
        when(request.pathParam("key")).thenReturn("key");

        assertThatThrownBy(() -> controller.get(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete() {
        CacheImpl<?> cache = mock(CacheImpl.class);
        caches.put("cache1", cache);

        var request = mock(Request.class);
        when(request.clientIP()).thenReturn("127.0.0.1");
        when(request.pathParam("name")).thenReturn("cache1");
        when(request.pathParam("key")).thenReturn("key");

        Response response = controller.delete(request);
        assertThat(response.status()).isEqualTo(HTTPStatus.OK);
        verify(cache).evict("key");
    }
}
