package core.framework.async;

import java.util.concurrent.Callable;

/**
 * @author neo
 */
@FunctionalInterface
public interface Task extends Callable<Void> {
    @Override
    default Void call() throws Exception {
        execute();
        return null;
    }

    void execute() throws Exception;    // not using Runnable to allow throw exception
}
