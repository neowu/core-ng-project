package core.framework.impl.scheduler;

import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.LogManager;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SchedulerTest {

    @Test
    public void testScheduleWithDelayFunction() throws InterruptedException {
        LogManager logManager = new LogManager();
        Scheduler scheduler = new Scheduler(new Executor(logManager), logManager);

        List<Long> results = new ArrayList<>();
        long baseTime = System.currentTimeMillis();
        scheduler.schedule("test", () -> {
            results.add(System.currentTimeMillis());
        }, Duration.of(20, ChronoUnit.MILLIS), () -> Duration.of(20, ChronoUnit.MILLIS));

        Thread.sleep(100);

        for(long time : results) {
            assertTrue(Math.abs(baseTime - time) < 2);
            baseTime = time;
        }
    }
}
