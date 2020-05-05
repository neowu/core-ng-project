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
        var pod = new PodList.Pod();
        pod.status = new PodList.PodStatus();
        pod.status.phase = "Running";
        var status = new PodList.ContainerStatus();
        status.state = new PodList.ContainerState();
        status.state.waiting = new PodList.ContainerStateWaiting();
        status.state.waiting.reason = "CrashLoopBackOff";
        status.state.waiting.message = "Back-off 5m0s restarting failed container";
        pod.status.containerStatuses = List.of(status);

        assertThat(job.check(pod)).isEqualTo(status.state.waiting.message);
    }
}
