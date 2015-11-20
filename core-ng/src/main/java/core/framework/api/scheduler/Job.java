package core.framework.api.scheduler;

/**
 * @author neo
 */
@FunctionalInterface
public interface Job {
    void execute() throws Exception;
}
