package app.monitor.job;

import app.monitor.kube.KubeClient;
import app.monitor.kube.PodList;
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

    public KubeMonitorJob(MessagePublisher<StatMessage> publisher, KubeClient kubeClient, List<String> namespaces) {
        this.publisher = publisher;
        this.kubeClient = kubeClient;
        this.namespaces = namespaces;
    }

    @Override
    public void execute(JobContext context) {
        try {
            for (String namespace : namespaces) {
                PodList pods = kubeClient.listPods(namespace);
                for (PodList.Pod pod : pods.items) {
                    String errorMessage = check(pod);
                    if (errorMessage != null) {
                        logger.warn("detected failed pod, pod={}", JSON.toJSON(pod));
                        publishPodFailure(pod, errorMessage);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            publishError(e);
        }
    }

    String check(PodList.Pod pod) {
        String phase = pod.status.phase;
        if ("Succeeded".equals(phase)) return null; // terminated in success
        if ("Failed".equals(phase) || "Unknown".equals(phase)) return "unexpected pod phase, phase=" + phase;
        if ("Pending".equals(phase)) {
            for (PodList.ContainerStatus status : pod.status.containerStatuses) {
                if (status.state.waiting != null && "ImagePullBackOff".equals(status.state.waiting.reason)) {
                    return status.state.waiting.message;
                }
            }
        }
        if ("Running".equals(phase)) {
            boolean allReady = true;
            for (PodList.ContainerStatus status : pod.status.containerStatuses) {
                if (status.state.waiting != null && "CrashLoopBackOff".equals(status.state.waiting.reason)) {
                    return status.state.waiting.message;
                }
                if (status.restartCount >= 5) {
                    return "pod restarted too many times, restart=" + status.restartCount;
                }
                if (status.state.terminated == null
                        && !Boolean.TRUE.equals(status.ready))
                    allReady = false;
            }
            if (allReady) return null;  // all running, all ready
        }
        if (Duration.between(pod.status.startTime, ZonedDateTime.now()).toSeconds() > 300) {
            return "pod took too long to be ready";
        }
        return null;
    }

    private void publishPodFailure(PodList.Pod pod, String errorMessage) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = Instant.now();
        message.result = "ERROR";
        message.app = app(pod);
        message.host = pod.metadata.name;
        message.errorCode = "POD_FAILURE";
        message.errorMessage = errorMessage;
        publisher.publish(message);
    }

    private String app(PodList.Pod pod) {
        Map<String, String> labels = pod.metadata.labels;
        if (labels == null) return pod.metadata.name;
        return labels.getOrDefault("app", pod.metadata.name);
    }

    private void publishError(Throwable e) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = "ERROR";
        message.app = "kubernetes";
        message.errorCode = "FAILED_TO_COLLECT";
        message.errorMessage = e.getMessage();
        publisher.publish(message);
    }
}
