package core.framework.impl.scheduler;


import org.assertj.core.api.Assertions;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class SchedulerTest {
    private Scheduler scheduler;
    private ScheduledExecutorService schedulerExecutor;
    private ExecutorService jobExecutor;

    @BeforeEach
    void createScheduler() {
        schedulerExecutor = mock(ScheduledExecutorService.class);
        jobExecutor = mock(ExecutorService.class);
        scheduler = new Scheduler(null, schedulerExecutor, jobExecutor);
    }

    @Test
    void next() {
        Assertions.assertThatThrownBy(() -> scheduler.next(previous -> null, ZonedDateTime.now()))
                  .hasMessageContaining("must be after previous");

        assertThat(scheduler.next(previous -> previous.plusHours(1), ZonedDateTime.now())).isNotNull();
    }

    @Test
    void scheduleTriggerTask() {
        TriggerTask task = new TriggerTask("trigger-job", null, previous -> previous.plusHours(1), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();
        scheduler.schedule(task, now, Clock.fixed(now.toInstant(), ZoneId.systemDefault()));

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
}
