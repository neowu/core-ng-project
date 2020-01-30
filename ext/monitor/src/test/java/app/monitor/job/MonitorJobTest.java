package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class MonitorJobTest {
    private MonitorJob job;
    private MessagePublisher<StatMessage> publisher;
    private Collector collector;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void createMonitorJob() {
        publisher = mock(MessagePublisher.class);
        collector = mock(Collector.class);
        job = new MonitorJob(collector, "app", "host", publisher);
    }

    @Test
    void execute() {
        when(collector.collect()).thenReturn(new Stats());
        job.execute(null);
        verify(publisher).publish(argThat(message -> "app".equals(message.app) && "OK".equals(message.result)));
    }

    @Test
    void failedToCollect() {
        when(collector.collect()).thenThrow(new Error("failed to connect"));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "app".equals(message.app)
                && "ERROR".equals(message.result)
                && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
