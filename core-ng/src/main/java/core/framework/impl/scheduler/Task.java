package core.framework.impl.scheduler;

import core.framework.scheduler.Job;

/**
 * @author neo
 */
public interface Task {
    String name();

    Job job();

    String trigger();
}
