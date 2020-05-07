package app.monitor.job;

import app.monitor.kube.KubeClient;
import app.monitor.kube.PodList;
import app.monitor.kube.PodListResponse;
import core.framework.internal.log.LogManager;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
                check(namespace);
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            publishError(e);
        }
    }

    private void check(String namespace) {
        PodListResponse response = kubeClient.listPods(namespace);
        List<String> failedPods = new ArrayList<>();
        List<PodList.Pod> pods = response.pods();
        for (PodList.Pod pod : pods) {
            String errorMessage = check(pod);
            if (errorMessage != null) {
                failedPods.add(pod.metadata.name);
                publishPodFailure(pod, errorMessage);
            }
        }
        if (!failedPods.isEmpty()) {
            logger.warn("detected failed pods, ns={}, pods={}, response={}", namespace, failedPods, response.body);
        }
    }

    String check(PodList.Pod pod) {
        String phase = pod.status.phase;
        if ("Succeeded".equals(phase)) return null; // terminated
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
        if (pod.status.startTime != null) { // startTime may not be populated yet if pod is just created
            Duration elapsed = Duration.between(pod.status.startTime, ZonedDateTime.now());
            if (elapsed.toSeconds() > 300) {
                return "pod is still not ready, elapsed=" + elapsed;
            }
        }
        return null;
    }

    private void publishPodFailure(PodList.Pod pod, String errorMessage) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = Instant.now();
        message.result = "ERROR";
        message.app = pod.metadata.labels.getOrDefault("app", pod.metadata.name);
        message.host = pod.metadata.name;
        message.errorCode = "POD_FAILURE";
        message.errorMessage = errorMessage;
        publisher.publish(message);
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
