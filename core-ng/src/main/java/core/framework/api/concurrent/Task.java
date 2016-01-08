package core.framework.api.concurrent;

/**
 * @author neo
 */
@FunctionalInterface
public interface Task {
    void execute() throws Exception;
}
