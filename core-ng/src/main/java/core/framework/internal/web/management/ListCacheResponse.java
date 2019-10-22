package core.framework.internal.web.management;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.List;

/**
 * @author neo
 */
public class ListCacheResponse {
    @NotNull
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
