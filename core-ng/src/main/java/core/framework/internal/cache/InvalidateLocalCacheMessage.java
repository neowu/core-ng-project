package core.framework.internal.cache;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class InvalidateLocalCacheMessage {
    @Property(name = "keys")
    public List<String> keys;

    @Property(name = "clientIP")
    public String clientIP;
}
