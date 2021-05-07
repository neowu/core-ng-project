package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.cache.CacheImpl;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.NotFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class CacheController {
    private final Map<String, CacheImpl<?>> caches;
    private final IPv4AccessControl accessControl = new IPv4AccessControl();

    public CacheController(Map<String, CacheImpl<?>> caches) {
        this.caches = caches;
    }

    public Response get(Request request) {
        accessControl.validate(request.clientIP());
        String name = request.pathParam("name");
        String key = request.pathParam("key");
        CacheImpl<?> cache = cache(name);
        Object value = cache.get(key).orElseThrow(() -> new NotFoundException("cache key not found, name=" + name + ", key=" + key));
        return Response.text(JSON.toJSON(value)).contentType(ContentType.APPLICATION_JSON);
    }

    public Response delete(Request request) {
        accessControl.validate(request.clientIP());
        String name = request.pathParam("name");
        String key = request.pathParam("key");
        CacheImpl<?> cache = cache(name);
        cache.evict(key);
        return Response.text(Strings.format("cache evicted, name={}, key={}", name, key));
    }

    public Response list(Request request) {
        accessControl.validate(request.clientIP());
        var response = new ListCacheResponse();
        response.caches = caches.values().stream().map(this::view).collect(Collectors.toList());
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    CacheImpl<?> cache(String name) {
        CacheImpl<?> cache = caches.get(name);
        if (cache == null) throw new NotFoundException("cache not found, name=" + name);
        return cache;
    }

    private ListCacheResponse.Cache view(CacheImpl<?> cache) {
        var view = new ListCacheResponse.Cache();
        view.name = cache.name;
        view.type = cache.cacheClass.getCanonicalName();
        view.duration = (int) cache.duration.getSeconds();
        return view;
    }
}
