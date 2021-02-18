package core.visualization.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.math.BigDecimal;
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
    public List<EdgeInfo> edges = new ArrayList<>();

    public static class EdgeInfo {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "actionName")
        public String actionName;

        @NotNull
        @Property(name = "elapsed")
        public BigDecimal elapsed;

        @NotNull
        @Property(name = "cpuTime")
        public BigDecimal cpuTime;

        @Property(name = "httpElapsed")
        public BigDecimal httpElapsed;

        @Property(name = "dbElapsed")
        public BigDecimal dbElapsed;

        @Property(name = "redisElapsed")
        public BigDecimal redisElapsed;

        @Property(name = "esElapsed")
        public BigDecimal esElapsed;

        @Property(name = "kafkaElapsed")
        public BigDecimal kafkaElapsed;

        @Property(name = "cacheHits")
        public Integer cacheHits;

        @Property(name = "errorCode")
        public String errorCode;

        @Property(name = "errorMessage")
        public String errorMessage;
    }
}
