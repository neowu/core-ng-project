package core.framework.impl.web.management;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class UpdateTopicRequest {
    @Property(name = "partitions")
    public Integer partitions;
}
