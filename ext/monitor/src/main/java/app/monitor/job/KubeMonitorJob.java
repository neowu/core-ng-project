package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class KubeMonitorJob implements Job {
    public final MessagePublisher<StatMessage> publisher;
    public final KubeClient kubeClient;
    public final List<String> namespaces;
    private final Logger logger = LoggerFactory.getLogger(KubeMonitorJob.class);

    public KubeMonitorJob(List<String> namespaces, KubeClient kubeClient, MessagePublisher<StatMessage> publisher) {
        this.publisher = publisher;
        this.kubeClient = kubeClient;
        this.namespaces = namespaces;
    }

    @Override
    public void execute(JobContext context) {
        try {
            var now = ZonedDateTime.now();
            for (String namespace : namespaces) {
                KubePodList pods = kubeClient.listPods(namespace);
                for (KubePodList.Pod pod : pods.items) {
                    String errorMessage = check(pod, now);
                    if (errorMessage != null) {
                        publishPodFailure(pod, errorMessage);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            publisher.publish(StatMessageFactory.failedToCollect(LogManager.APP_NAME, null, e));
        }
    }

    String check(KubePodList.Pod pod, ZonedDateTime now) {
        if (pod.metadata.deletionTimestamp != null) {
            Duration elapsed = Duration.between(pod.metadata.deletionTimestamp, now);
            if (elapsed.toSeconds() >= 300) {
                return "pod is still in deletion, elapsed=" + elapsed;
            }
            return null;
        }

        String phase = pod.status.phase;
        if ("Succeeded".equals(phase)) return null; // terminated
        if ("Failed".equals(phase) || "Unknown".equals(phase)) return "unexpected pod phase, phase=" + phase;
        if ("Pending".equals(phase)) {
            // newly created pod may not have container status yet, containerStatuses is initialized as empty
            for (KubePodList.ContainerStatus status : pod.status.containerStatuses) {
                if (status.state.waiting != null && "ImagePullBackOff".equals(status.state.waiting.reason)) {
                    return "ImagePullBackOff: " + status.state.waiting.message;
                }
            }
            // for unschedulable pod
            for (KubePodList.PodCondition condition : pod.status.conditions) {
                if ("PodScheduled".equals(condition.type) && "False".equals(condition.status) && Duration.between(condition.lastTransitionTime, now).toSeconds() >= 300) {
                    return condition.reason + ": " + condition.message;
                }
            }
        }
        if ("Running".equals(phase)) {
            boolean ready = true;
            for (KubePodList.ContainerStatus status : pod.status.containerStatuses) {
                if (status.state.waiting != null && "CrashLoopBackOff".equals(status.state.waiting.reason)) {
                    return "CrashLoopBackOff: " + status.state.waiting.message;
                }
                boolean containerReady = Boolean.TRUE.equals(status.ready);
                if (!containerReady && status.lastState != null && status.lastState.terminated != null) {
                    var terminated = status.lastState.terminated;
                    return "pod was terminated, reason=" + terminated.reason + ", exitCode=" + terminated.exitCode;
                }
                if (!containerReady) {
                    ready = false;
                }
            }
            if (ready) return null;  // all running, all ready
        }
        ZonedDateTime startTime = pod.status.startTime != null ? pod.status.startTime : pod.metadata.creationTimestamp;  // startTime may not be populated yet if pod is just created
        Duration elapsed = Duration.between(startTime, now);
        if (elapsed.toSeconds() >= 300) {
            // can be: 1) took long to be ready after start, or 2) readiness check failed in the middle run
            return "pod is not in ready state, uptime=" + elapsed;
        }
        return null;
    }

    private void publishPodFailure(KubePodList.Pod pod, String errorMessage) {
        var now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = "ERROR";
        message.app = pod.metadata.labels.getOrDefault("app", pod.metadata.name);
        message.host = pod.metadata.name;
        message.errorCode = "POD_FAILURE";
        message.errorMessage = errorMessage;
        message.info = Map.of("pod", JSON.toJSON(pod));
        publisher.publish(message);
    }
}
