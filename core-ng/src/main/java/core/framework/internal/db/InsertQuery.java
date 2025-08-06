package core.framework.internal.db;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
final class InsertQuery<T> {
    final String insertSQL;
    final String insertIgnoreSQL;
    final String upsertSQL;
    @Nullable
    final String generatedColumn;
    private final InsertQueryParamBuilder<T> paramBuilder;

    InsertQuery(String insertSQL, String insertIgnoreSQL, String upsertSQL, @Nullable String generatedColumn, InsertQueryParamBuilder<T> paramBuilder) {
        this.insertSQL = insertSQL;
        this.insertIgnoreSQL = insertIgnoreSQL;
        this.upsertSQL = upsertSQL;
        this.generatedColumn = generatedColumn;
        this.paramBuilder = paramBuilder;
    }

    Object[] params(T entity) {
        return paramBuilder.params(entity);
    }
}
