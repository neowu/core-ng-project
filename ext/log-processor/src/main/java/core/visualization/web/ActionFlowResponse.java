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
    @Property(name = "nodes")
    public List<Node> nodes = new ArrayList<>();

    @NotNull
    @Property(name = "edges")
    public List<Edge> edges = new ArrayList<>();

    public static class Node {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "elapsed")
        public Integer elapsed;

        //        @NotNull
        @Property(name = "cpuTime")
        public Integer cpuTime;

        //        @NotNull
        @Property(name = "httpElapsed")
        public Integer httpElapsed;

        @Property(name = "dbElapsed")
        public Integer dbElapsed;

        @Property(name = "redisElapsed")
        public Integer redisElapsed;

        @Property(name = "esElapsed")
        public Integer esElapsed;

        @Property(name = "kafkaElapsed")
        public Integer kafkaElapsed;

        @Property(name = "cacheHits")
        public Integer cacheHits;
    }

    public static class Edge {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "errorCode")
        public String errorCode;

        @NotNull
        @Property(name = "errorMessage")
        public String errorMessage;
    }
}
