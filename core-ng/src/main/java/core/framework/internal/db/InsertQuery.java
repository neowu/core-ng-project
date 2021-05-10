package core.framework.internal.db;

/**
 * @author neo
 */
final class InsertQuery<T> {
    final String sql;
    final String generatedColumn;
    private final InsertQueryParamBuilder<T> paramBuilder;

    InsertQuery(String sql, String generatedColumn, InsertQueryParamBuilder<T> paramBuilder) {
        this.sql = sql;
        this.generatedColumn = generatedColumn;
        this.paramBuilder = paramBuilder;
    }

    Object[] params(T entity) {
        return paramBuilder.params(entity);
    }

    String insertIgnoreSQL() {
        // due to insert ignore will be used less frequently, so not to build sql in advance to reduce memory footprint
        // replace first INSERT with INSERT IGNORE
        // new StringBuilder(str) will reserve str.length+16 as capacity, so insert will not trigger expansion
        return new StringBuilder(sql).insert(6, " IGNORE").toString();
    }
}
