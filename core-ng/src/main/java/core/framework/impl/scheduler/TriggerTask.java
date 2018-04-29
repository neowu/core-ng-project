package core.framework.impl.scheduler;

import core.framework.scheduler.Job;
import core.framework.scheduler.Trigger;
import core.framework.util.Strings;

import java.time.ZoneId;

/**
 * @author neo
 */
public class TriggerTask implements Task {
    final Trigger trigger;
    private final String name;
    private final Job job;
    private final ZoneId zoneId;

    TriggerTask(String name, Job job, Trigger trigger, ZoneId zoneId) {
        this.name = name;
        this.trigger = trigger;
        this.job = job;
        this.zoneId = zoneId;
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
    public String trigger() {
        return Strings.format("{}[{}]", trigger, zoneId);
    }
}
