package core.framework.internal.db;

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

    String insertIgnoreSQL() {
        // due to insert ignore will be used less frequently, so not to build sql in advance to reduce memory footprint
        // replace first INSERT with INSERT IGNORE
        // new StringBuilder(str) will reserve str.length+16 as capacity, so insert will not trigger expansion
        return new StringBuilder(sql).insert(6, " IGNORE").toString();
    }
}
