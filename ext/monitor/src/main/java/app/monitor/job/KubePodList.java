package app.monitor.job;

import core.framework.api.json.Property;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class KubePodList {  // refer to https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.18/#list-pod-v1-core
    @Property(name = "items")
    public List<Pod> items;

    public static class Pod {
        @Property(name = "metadata")
        public ObjectMeta metadata = new ObjectMeta();

        @Property(name = "status")
        public PodStatus status = new PodStatus();
    }

    public static class ObjectMeta {
        @Property(name = "namespace")
        public String namespace;

        @Property(name = "name")
        public String name;

        @Property(name = "generateName")
        public String generateName;

        @Property(name = "creationTimestamp")
        public ZonedDateTime creationTimestamp;

        @Property(name = "deletionTimestamp")
        public ZonedDateTime deletionTimestamp;

        @Property(name = "deletionGracePeriodSeconds")
        public Integer deletionGracePeriodSeconds;

        @Property(name = "labels")
        public Map<String, String> labels = Map.of();
    }

    public static class PodStatus {
        @Property(name = "phase")
        public String phase;

        @Property(name = "conditions")
        public List<PodCondition> conditions = List.of();

        @Property(name = "initContainerStatuses")
        public List<ContainerStatus> initContainerStatuses;

        @Property(name = "containerStatuses")
        public List<ContainerStatus> containerStatuses = List.of(); // kube api may return null for newly created pending pod

        @Property(name = "startTime")
        public ZonedDateTime startTime;

        @Property(name = "hostIP")
        public String hostIP;

        @Property(name = "podIP")
        public String podIP;

        @Property(name = "reason")
        public String reason;

        @Property(name = "message")
        public String message;
    }

    public static class ContainerStatus {
        @Property(name = "image")
        public String image;

        @Property(name = "name")
        public String name;

        @Property(name = "ready")
        public Boolean ready;   // whether pass readiness probe

        @Property(name = "restartCount")
        public Integer restartCount = 0;

        @Property(name = "state")
        public ContainerState state = new ContainerState();

        @Property(name = "lastState")
        public ContainerState lastState;
    }

    public static class ContainerState {
        @Property(name = "running")
        public ContainerStateRunning running;

        @Property(name = "waiting")
        public ContainerStateWaiting waiting;

        @Property(name = "terminated")
        public ContainerStateTerminated terminated;
    }

    public static class ContainerStateRunning {
        @Property(name = "startedAt")
        public ZonedDateTime startedAt;
    }

    public static class ContainerStateWaiting {
        @Property(name = "reason")
        public String reason;

        @Property(name = "message")
        public String message;
    }

    public static class ContainerStateTerminated {
        @Property(name = "exitCode")
        public Integer exitCode;

        @Property(name = "reason")
        public String reason;

        @Property(name = "message")
        public String message;
    }

    public static class PodCondition {
        @Property(name = "type")
        public String type;

        @Property(name = "status")
        public String status;

        @Property(name = "lastProbeTime")
        public ZonedDateTime lastProbeTime;

        @Property(name = "lastTransitionTime")
        public ZonedDateTime lastTransitionTime;

        @Property(name = "reason")
        public String reason;

        @Property(name = "message")
        public String message;
    }
}
