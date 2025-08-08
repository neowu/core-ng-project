package core.framework.internal.db;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public interface UpdateQuery<T> {
    Statement update(T entity, boolean partial, @Nullable String where, Object @Nullable [] params);

    class Statement {
        final String sql;
        final Object[] params;

        Statement(String sql, Object... params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
