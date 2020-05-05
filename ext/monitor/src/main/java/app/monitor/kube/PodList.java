package app.monitor.kube;

import core.framework.api.json.Property;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class PodList {  // refer to https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.18/#list-pod-v1-core
    @Property(name = "items")
    public List<Pod> items;

    public static class Pod {
        @Property(name = "metadata")
        public ObjectMeta metadata;

        @Property(name = "status")
        public PodStatus status;
    }

    public static class ObjectMeta {
        @Property(name = "namespace")
        public String namespace;

        @Property(name = "name")
        public String name;

        @Property(name = "labels")
        public Map<String, String> labels;
    }

    public static class PodStatus {
        @Property(name = "phase")
        public String phase;

        @Property(name = "containerStatuses")
        public List<ContainerStatus> containerStatuses;

        @Property(name = "startTime")
        public ZonedDateTime startTime;
    }

    public static class ContainerStatus {
        @Property(name = "ready")
        public Boolean ready;   // whether pass readiness probe

        @Property(name = "restartCount")
        public Integer restartCount;

        @Property(name = "state")
        public ContainerState state;
    }

    public static class ContainerState {
        // there are running / terminated / waiting states, right now only waiting is used
        @Property(name = "waiting")
        public ContainerStateWaiting waiting;
    }

    public static class ContainerStateWaiting {
        @Property(name = "reason")
        public String reason;

        @Property(name = "message")
        public String message;
    }
}
