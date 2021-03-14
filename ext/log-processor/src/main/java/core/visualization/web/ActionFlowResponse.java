package core.visualization.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allison
 */
public class ActionFlowResponse {
    @NotNull
    @Property(name = "graph")
    public String graph;

    @NotNull
    @Property(name = "edges")
    public List<Edge> edges = new ArrayList<>();

    public static class Edge {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "html")
        public String html;
    }
}
