package core.framework.api.web;

/**
 * @author neo
 */
@FunctionalInterface
public interface Controller {
    Response execute(Request request) throws Exception;
}
