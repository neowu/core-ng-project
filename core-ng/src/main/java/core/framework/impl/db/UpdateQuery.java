package core.framework.impl.db;

/**
 * @author neo
 */
public interface UpdateQuery<T> {
    Statement update(T entity);

    class Statement {
        final String sql;
        final Object[] params;

        Statement(String sql, Object... params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
