package core.framework.internal.db;

/**
 * @author neo
 */
interface InsertQueryParamBuilder<T> {
    Object[] params(T entity);
}
