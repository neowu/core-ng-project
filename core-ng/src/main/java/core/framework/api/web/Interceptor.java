package core.framework.api.web;

/**
 * @author neo
 */
@FunctionalInterface
public interface Interceptor {
    Response intercept(Invocation invocation) throws Exception;
}
