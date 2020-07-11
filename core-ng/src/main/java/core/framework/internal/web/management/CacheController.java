package core.framework.internal.web.management;

import core.framework.http.ContentType;
import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.CacheManager;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.NotFoundException;

import java.util.stream.Collectors;

/**
 * @author neo
 */
public class CacheController {
    private final CacheManager cacheManager;
    private final IPv4AccessControl accessControl = new IPv4AccessControl();

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
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
        ListCacheResponse response = new ListCacheResponse();
        response.caches = cacheManager.caches().stream().map(this::view).collect(Collectors.toList());
        return Response.bean(response);
    }

    private CacheImpl<?> cache(String name) {
        return cacheManager.get(name).orElseThrow(() -> new NotFoundException("cache not found, name=" + name));
    }

    private ListCacheResponse.Cache view(CacheImpl<?> cache) {
        var view = new ListCacheResponse.Cache();
        view.name = cache.name;
        view.type = cache.cacheClass.getCanonicalName();
        view.duration = (int) cache.duration.getSeconds();
        return view;
    }
}
