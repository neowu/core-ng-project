package core.framework.internal.async;

import core.framework.async.Task;

import java.util.concurrent.Callable;

/**
 * @author neo
 */
public record CallableTask(Task task) implements Callable<Void> {
    static Class<?> taskClass(Callable<?> task) {
        // task can only be Callable or Task
        if (task instanceof CallableTask callableTask) return callableTask.task.getClass();
        return task.getClass();
    }

    @Override
    public Void call() throws Exception {
        task.execute();
        return null;
    }
}
