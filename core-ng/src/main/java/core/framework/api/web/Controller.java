package core.framework.api.web;

/**
 * @author neo
 */
public interface Controller {
    Response execute(Request request) throws Exception;
}
