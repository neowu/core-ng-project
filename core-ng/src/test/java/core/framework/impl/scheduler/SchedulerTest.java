package core.framework.impl.scheduler;


import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.scheduler.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class SchedulerTest {
    private Scheduler scheduler;
    private ScheduledExecutorService schedulerExecutor;
    private ExecutorService jobExecutor;
    private LogManager logManager;

    @BeforeEach
    void createScheduler() {
        schedulerExecutor = mock(ScheduledExecutorService.class);
        jobExecutor = mock(ExecutorService.class);
        logManager = mock(LogManager.class);
        scheduler = new Scheduler(logManager, schedulerExecutor, jobExecutor);
    }

    @Test
    void next() {
        assertThatThrownBy(() -> scheduler.next(previous -> null, ZonedDateTime.now()))
                .hasMessageContaining("must be after previous");

        assertThat(scheduler.next(previous -> previous.plusHours(1), ZonedDateTime.now())).isNotNull();
    }

    @Test
    void scheduleTriggerTask() {
        ZonedDateTime now = ZonedDateTime.now();
        scheduler.clock = Clock.fixed(now.toInstant(), ZoneId.systemDefault());

        TriggerTask task = new TriggerTask("trigger-job", null, previous -> previous.plusHours(1), ZoneId.systemDefault());
        scheduler.schedule(task, now);

        ArgumentCaptor<Runnable> scheduledTask = ArgumentCaptor.forClass(Runnable.class);
        verify(schedulerExecutor).schedule(scheduledTask.capture(), eq(0L), eq(TimeUnit.NANOSECONDS));

        scheduledTask.getValue().run();
        verify(schedulerExecutor).schedule(scheduledTask.capture(), eq(Duration.ofHours(1).toNanos()), eq(TimeUnit.NANOSECONDS));
        verify(jobExecutor).submit((Callable<?>) any(Callable.class));
    }

    @Test
    void scheduleFixedRateTask() {
        Duration rate = Duration.ofHours(1);
        scheduler.schedule(new FixedRateTask("hourly-job", null, rate));

        ArgumentCaptor<Runnable> scheduledTask = ArgumentCaptor.forClass(Runnable.class);
        verify(schedulerExecutor).scheduleAtFixedRate(scheduledTask.capture(), anyLong(), eq(rate.toNanos()), eq(TimeUnit.NANOSECONDS));

        scheduledTask.getValue().run();
        verify(jobExecutor).submit((Callable<?>) any(Callable.class));
    }

    @Test
    void executeTaskWithTriggerError() {
        TriggerTask task = new TriggerTask("trigger-job", null, previous -> previous, ZoneId.systemDefault());
        scheduler.executeTask(task, ZonedDateTime.now());

        verify(jobExecutor, never()).submit((Callable<?>) any(Callable.class));
    }

    @Test
    void triggerNow() throws Exception {
        scheduler.addFixedRateTask("hourly-job", new TestJob(), Duration.ofHours(1));
        scheduler.triggerNow("hourly-job");

        ArgumentCaptor<Callable<?>> task = ArgumentCaptor.forClass(Callable.class);
        verify(jobExecutor).submit(task.capture());

        ActionLog actionLog = new ActionLog(null);
        when(logManager.begin(anyString())).thenReturn(actionLog);

        task.getValue().call();

        assertThat(actionLog.trace).isTrue();
    }

    public static class TestJob implements Job {
        @Override
        public void execute() {

        }
    }
}
