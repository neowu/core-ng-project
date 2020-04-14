package core.framework.async;

/**
 * @author neo
 */
@FunctionalInterface
public interface Task {
    void execute() throws Exception;    // not using Runnable to allow throw exception
}
