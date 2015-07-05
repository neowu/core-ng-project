package core.framework.api.web;

/**
 * @author neo
 */
public interface Interceptor {
    Response intercept(Invocation invocation) throws Exception;
}
