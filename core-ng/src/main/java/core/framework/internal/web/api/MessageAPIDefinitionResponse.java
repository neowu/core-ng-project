package core.framework.internal.web.api;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class MessageAPIDefinitionResponse {
    @Property(name = "app")
    public String app;

    @Property(name = "version")
    public String version;      // used to fast compare

    @Property(name = "topics")
    public List<Topic> topics;

    @Property(name = "types")
    public List<APIType> types;

    public static class Topic {
        @Property(name = "name")
        public String name;

        @Property(name = "messageType")
        public String messageType;
    }
}
