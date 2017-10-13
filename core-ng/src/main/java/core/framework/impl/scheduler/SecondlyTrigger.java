package core.framework.impl.scheduler;


import core.framework.scheduler.Job;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public final class SecondlyTrigger implements Trigger {
    private static final long NANO_PER_SECOND = 1000_000_000L;
    private final String name;
    private final Job job;
    private final Duration rate;

    public SecondlyTrigger(String name, Job job, int rateInSeconds) {
        this.name = name;
        this.job = job;
        this.rate = Duration.ofSeconds(rateInSeconds);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Job job() {
        return job;
    }

    @Override
    public void schedule(Scheduler scheduler) {
        LocalTime now = LocalTime.now();
        Duration delay = delay(now.getSecond(), now.getNano());
        scheduler.schedule(this, delay, rate);
    }

    Duration delay(int currentSecond, int currentNano) {
        long currentSecondNano = currentSecond * NANO_PER_SECOND + currentNano;
        long mod = currentSecondNano % (rate.getSeconds() * NANO_PER_SECOND + rate.getNano());
        if (mod == 0) return Duration.ZERO;
        return rate.minus(Duration.ofNanos(mod));
    }

    @Override
    public String frequency() {
        return "secondly@" + rate;
    }
}
