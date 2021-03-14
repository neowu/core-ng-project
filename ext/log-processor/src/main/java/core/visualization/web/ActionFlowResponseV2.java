package core.visualization.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allison
 */
public class ActionFlowResponseV2 {
    @NotNull
    @Property(name = "graph")
    public String graph;

    @NotNull
    @Property(name = "edges")
    public List<EdgeInfo> edges = new ArrayList<>();

    public static class EdgeInfo {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "html")
        public String html;
    }
}
