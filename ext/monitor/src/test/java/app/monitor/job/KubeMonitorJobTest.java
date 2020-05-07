package app.monitor.job;

import app.monitor.kube.PodList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KubeMonitorJobTest {
    private KubeMonitorJob job;

    @BeforeEach
    void createKubeMonitorJob() {
        job = new KubeMonitorJob(null, null, null);
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

        assertThat(job.check(pod, ZonedDateTime.now())).isEqualTo(status.state.waiting.message);
    }

    @Test
    void checkWithNotReady() {
        var startTime = ZonedDateTime.now();

        var pod = pod("Pending");
        pod.status.containerStatuses = null;
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

        assertThat(job.check(pod, ZonedDateTime.now())).isEqualTo(status.state.waiting.message);
    }

    private PodList.Pod pod(String phase) {
        var pod = new PodList.Pod();
        pod.status.phase = phase;
        pod.status.containerStatuses = List.of(new PodList.ContainerStatus());
        return pod;
    }
}
