package core.framework.internal.resource;

/**
 * @author neo
 */
public interface ResourceValidator<T> {
    boolean validate(T resource) throws Throwable;
}
