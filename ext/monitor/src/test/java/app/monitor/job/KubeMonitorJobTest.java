package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class KubeMonitorJobTest {
    @Mock
    KubeClient kubeClient;
    @Mock
    MessagePublisher<StatMessage> publisher;
    private KubeMonitorJob job;

    @BeforeEach
    void createKubeMonitorJob() {
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
        KubePodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new KubePodList.ContainerStateWaiting();
        status.state.waiting.reason = "CrashLoopBackOff";
        status.state.waiting.message = "Back-off 5m0s restarting failed container";

        assertThat(job.check(pod, ZonedDateTime.now()))
            .isEqualTo("CrashLoopBackOff: Back-off 5m0s restarting failed container");
    }

    @Test
    void checkWithOOMKill() {
        var pod = pod("Running");
        KubePodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        var lastState = new KubePodList.ContainerState();
        lastState.terminated = new KubePodList.ContainerStateTerminated();
        lastState.terminated.reason = "OOMKilled";
        lastState.terminated.exitCode = 137;
        status.lastState = lastState;

        assertThat(job.check(pod, ZonedDateTime.now()))
            .isEqualTo("pod was terminated, reason=OOMKilled, exitCode=137");
    }

    @Test
    void checkWithFailedToStart() {
        var pod = pod("Running");
        KubePodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        var lastState = new KubePodList.ContainerState();
        lastState.terminated = new KubePodList.ContainerStateTerminated();
        lastState.terminated.reason = "Error";
        lastState.terminated.exitCode = 1;      // failed to start, App.java return exitCode=1 if any exception
        status.lastState = lastState;

        assertThat(job.check(pod, ZonedDateTime.now()))
            .isEqualTo("pod was terminated, reason=Error, exitCode=1");

        status.ready = Boolean.TRUE;
        assertThat(job.check(pod, ZonedDateTime.now())).isNull();
    }

    @Test
    void checkWithNotReady() {
        var startTime = ZonedDateTime.now();

        var pod = pod("Pending");
        pod.metadata.creationTimestamp = startTime;

        assertThat(job.check(pod, startTime.plusMinutes(1))).isNull();
        assertThat(job.check(pod, startTime.plusMinutes(5))).isEqualTo("pod is not in ready state, uptime=PT5M");

        pod = pod("Running");
        pod.status.containerStatuses.get(0).ready = Boolean.FALSE;
        pod.status.startTime = startTime;

        assertThat(job.check(pod, startTime.plusMinutes(1))).isNull();
        assertThat(job.check(pod, startTime.plusMinutes(5))).isEqualTo("pod is not in ready state, uptime=PT5M");
    }

    @Test
    void checkWithImagePullBackOff() {
        var pod = pod("Pending");
        KubePodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new KubePodList.ContainerStateWaiting();
        status.state.waiting.reason = "ImagePullBackOff";
        status.state.waiting.message = "Back-off pulling image \"gcr.io/project/ops/debug:latest\"";

        assertThat(job.check(pod, ZonedDateTime.now()))
            .contains(status.state.waiting.reason)
            .contains(status.state.waiting.message);
    }

    @Test
    void checkWithUnschedulable() {
        KubePodList.Pod pod = JSON.fromJSON(KubePodList.Pod.class, ClasspathResources.text("kube-monitor-test/unschedulable.json"));

        assertThat(job.check(pod, ZonedDateTime.parse("2021-11-23T14:52:34Z")))
            .contains("Unschedulable: 0/4 nodes are available: 1 Insufficient cpu, 1 node(s) didn't match Pod's node affinity/selector,");
    }

    @Test
    void publishError() throws IOException {
        when(kubeClient.listPods("ns")).thenThrow(new Error("mock"));
        job.execute(null);
        verify(publisher).publish(argThat(message -> LogManager.APP_NAME.equals(message.app)
                                                     && "ERROR".equals(message.result)
                                                     && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }

    private KubePodList.Pod pod(String phase) {
        var pod = new KubePodList.Pod();
        pod.status.phase = phase;
        pod.status.containerStatuses = List.of(new KubePodList.ContainerStatus());
        return pod;
    }
}
