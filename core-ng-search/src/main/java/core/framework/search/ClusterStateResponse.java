package core.framework.search;

import core.framework.api.json.Property;

import java.util.Map;

/**
 * @author neo
 */
public class ClusterStateResponse {
    @Property(name = "cluster_name")
    public String clusterName;

    @Property(name = "metadata")
    public Metadata metadata;

    public enum IndexState {
        @Property(name = "open")
        OPEN,
        @Property(name = "close")
        CLOSE,
    }

    public static class Metadata {
        @Property(name = "indices")
        public Map<String, Index> indices;
    }

    public static class Index {
        @Property(name = "state")
        public IndexState state;
    }
}
