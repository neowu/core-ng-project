package core.framework.internal.web.sys;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class ListCacheResponse {
    @Property(name = "caches")
    public List<Cache> caches;

    public static class Cache {
        @Property(name = "name")
        public String name;
        @Property(name = "type")
        public String type;
        @Property(name = "duration")
        public Integer duration;
    }
}
