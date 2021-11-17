package core.framework.internal.module;

import core.framework.async.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public final class StartupHook {
    // startup has 2 stages, initialize() is for client initialization, e.g. kafka client,
    // start() is to start actual process, like scheduler/listener/etc, those processors may depend on client requires initialize()

    public List<Task> initialize = new ArrayList<>();
    public List<Task> start = new ArrayList<>();

    public void initialize() throws Exception {
        for (Task task : initialize) {
            task.execute();
        }
        initialize = null;  // release memory
    }

    public void start() throws Exception {
        for (Task task : start) {
            task.execute();
        }
        start = null;   // release memory
    }
}
