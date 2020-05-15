package app.monitor.job;

import app.monitor.kube.KubeClient;
import app.monitor.kube.PodList;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class KubeMonitorJobTest {
    @Mock
    KubeClient kubeClient;
    @Mock
    MessagePublisher<StatMessage> publisher;
    private KubeMonitorJob job;

    @BeforeEach
    void createKubeMonitorJob() {
        MockitoAnnotations.initMocks(this);
        job = new KubeMonitorJob(List.of("ns"), kubeClient, publisher);
    }

    @Test
    void checkWithTermination() {
        var deleteTime = ZonedDateTime.now();
        var pod = pod("Pending");
        pod.metadata.deletionTimestamp = deleteTime;

        assertThat(job.check(pod, deleteTime.plusMinutes(1))).isNull();
        assertThat(job.check(pod, deleteTime.plusMinutes(5))).startsWith("pod is still in deletion, elapsed=");
    }

    @Test
    void checkWithCrashLoopBackOff() {
        var pod = pod("Running");
        PodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new PodList.ContainerStateWaiting();
        status.state.waiting.reason = "CrashLoopBackOff";
        status.state.waiting.message = "Back-off 5m0s restarting failed container";

        assertThat(job.check(pod, ZonedDateTime.now()))
                .contains(status.state.waiting.reason)
                .contains(status.state.waiting.message);
    }

    @Test
    void checkWithNotReady() {
        var startTime = ZonedDateTime.now();

        var pod = pod("Pending");
        pod.metadata.creationTimestamp = startTime;

        assertThat(job.check(pod, startTime.plusMinutes(1))).isNull();
        assertThat(job.check(pod, startTime.plusMinutes(5))).isEqualTo("pod is still not ready, elapsed=PT5M");

        pod = pod("Running");
        pod.status.containerStatuses.get(0).ready = false;
        pod.status.startTime = startTime;

        assertThat(job.check(pod, startTime.plusMinutes(1))).isNull();
        assertThat(job.check(pod, startTime.plusMinutes(5))).isEqualTo("pod is still not ready, elapsed=PT5M");
    }

    @Test
    void checkWithTooManyRestarts() {
        var pod = pod("Running");
        pod.status.containerStatuses.get(0).restartCount = 5;

        assertThat(job.check(pod, ZonedDateTime.now())).isEqualTo("pod restarted too many times, restart=5");
    }

    @Test
    void checkWithImagePullBackOff() {
        var pod = pod("Pending");
        PodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new PodList.ContainerStateWaiting();
        status.state.waiting.reason = "ImagePullBackOff";
        status.state.waiting.message = "Back-off pulling image \"gcr.io/project/ops/debug:latest\"";

        assertThat(job.check(pod, ZonedDateTime.now()))
                .contains(status.state.waiting.reason)
                .contains(status.state.waiting.message);
    }

    @Test
    void publishError() {
        when(kubeClient.listPods("ns")).thenThrow(new Error("mock"));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "kubernetes".equals(message.app)
                && "ERROR".equals(message.result)
                && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }

    private PodList.Pod pod(String phase) {
        var pod = new PodList.Pod();
        pod.status.phase = phase;
        pod.status.containerStatuses = List.of(new PodList.ContainerStatus());
        return pod;
    }
}
