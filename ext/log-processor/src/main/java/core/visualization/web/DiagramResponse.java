package core.visualization.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allison
 */
public class DiagramResponse {
    @NotNull
    @Property(name = "graph")
    public String graph;

    @NotNull
    @Property(name = "tooltips")
    public List<Tooltip> tooltips = new ArrayList<>();

    public static class Tooltip {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "html")
        public String html;
    }
}
