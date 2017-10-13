package core.framework.impl.web.management;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class CacheView {
    @Property(name = "name")
    public String name;
    @Property(name = "type")
    public String type;
    @Property(name = "duration")
    public Integer duration;
}
