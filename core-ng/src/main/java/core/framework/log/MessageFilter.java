package core.framework.log;

/**
 * @author neo
 */
@FunctionalInterface
public interface MessageFilter {
    String filter(String logger, String message);
}
