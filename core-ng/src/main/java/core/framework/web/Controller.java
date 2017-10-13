package core.framework.web;

/**
 * @author neo
 */
@FunctionalInterface
public interface Controller {
    Response execute(Request request) throws Exception;
}
