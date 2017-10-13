package core.framework.web;

/**
 * @author neo
 */
@FunctionalInterface
public interface Interceptor {
    Response intercept(Invocation invocation) throws Exception;
}
