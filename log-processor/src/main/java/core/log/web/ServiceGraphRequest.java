package core.log.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.List;

/**
 * @author neo
 */
public class ServiceGraphRequest {
    @NotNull
    @Property(name = "serviceURLs")
    public List<String> serviceURLs;

    @Property(name = "appNodeStyle")
    public String appNodeStyle;

    @Property(name = "messageNodeStyle")
    public String messageNodeStyle;

    @Property(name = "messageEdgeStyle")
    public String messageEdgeStyle;

    @Property(name = "serviceNodeStyle")
    public String serviceNodeStyle;

    @Property(name = "serviceEdgeStyle")
    public String serviceEdgeStyle;
}
