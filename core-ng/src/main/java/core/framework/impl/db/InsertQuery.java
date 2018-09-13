package core.framework.impl.db;

import java.util.function.Function;

/**
 * @author neo
 */
final class InsertQuery<T> {
    final String sql;
    final String generatedColumn;
    private final Function<T, Object[]> paramBuilder;

    InsertQuery(String sql, String generatedColumn, Function<T, Object[]> paramBuilder) {
        this.sql = sql;
        this.generatedColumn = generatedColumn;
        this.paramBuilder = paramBuilder;
    }

    Object[] params(T entity) {
        return paramBuilder.apply(entity);
    }
}
