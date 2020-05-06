package app.monitor.job;

import app.monitor.kube.PodList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void checkWithCrashLoopBackOff() {
        var pod = pod("Running");
        PodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new PodList.ContainerStateWaiting();
        status.state.waiting.reason = "CrashLoopBackOff";
        status.state.waiting.message = "Back-off 5m0s restarting failed container";

        assertThat(job.check(pod)).isEqualTo(status.state.waiting.message);
    }

    @Test
    void checkWithTermination() {
        var pod = pod("Running");
        PodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.terminated = new PodList.ContainerStateTerminated();
        status.state.terminated.exitCode = 0;

        assertThat(job.check(pod)).isNull();
    }

    @Test
    void checkWithImagePullBackOff() {
        var pod = pod("Pending");
        PodList.ContainerStatus status = pod.status.containerStatuses.get(0);
        status.state.waiting = new PodList.ContainerStateWaiting();
        status.state.waiting.reason = "ImagePullBackOff";
        status.state.waiting.message = "Back-off pulling image \"gcr.io/project/ops/debug:latest\"";

        assertThat(job.check(pod)).isEqualTo(status.state.waiting.message);
    }

    private PodList.Pod pod(String phase) {
        var pod = new PodList.Pod();
        pod.status = new PodList.PodStatus();
        pod.status.phase = phase;
        var status = new PodList.ContainerStatus();
        status.restartCount = 0;
        status.state = new PodList.ContainerState();
        pod.status.containerStatuses = List.of(status);
        return pod;
    }
}
