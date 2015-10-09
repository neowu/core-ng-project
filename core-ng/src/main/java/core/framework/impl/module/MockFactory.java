package core.framework.impl.module;

/**
 * @author neo
 */
public interface MockFactory {
    <T> T create(Class<T> instanceClass, Object... params);
}
