package core.framework.internal.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.internal.reflect.Classes;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author neo
 */
final class SelectQuery<T> {
    final String getSQL;
    final Dialect dialect;
    private final String table;
    private final String columns;
    int primaryKeyColumns;

    SelectQuery(Class<T> entityClass, Dialect dialect) {
        table = entityClass.getDeclaredAnnotation(Table.class).name();
        List<Field> fields = Classes.instanceFields(entityClass);
        columns = columns(fields);
        getSQL = getSQL(fields);
        this.dialect = dialect;
    }

    private String getSQL(List<Field> fields) {
        var builder = new StringBuilder(64);
        builder.append("SELECT ").append(columns).append(" FROM ").append(table).append(" WHERE ");
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column column = field.getDeclaredAnnotation(Column.class);
                if (primaryKeyColumns > 0) builder.append(" AND ");
                builder.append(column.name()).append(" = ?");
                primaryKeyColumns++;
            }
        }
        return builder.toString();
    }

    private String columns(List<Field> fields) {
        var builder = new StringBuilder();
        int index = 0;
        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            index++;
        }
        return builder.toString();
    }

    String fetchSQL(StringBuilder where, String sort, Integer skip, Integer limit) {
        return sql(columns, where, null, sort, skip, limit);
    }

    String sql(String projection, StringBuilder where, String groupBy, String sort, Integer skip, Integer limit) {
        var builder = new StringBuilder(64);
        builder.append("SELECT ").append(projection).append(" FROM ").append(table);
        if (!where.isEmpty()) builder.append(" WHERE ").append(where);
        if (groupBy != null) builder.append(" GROUP BY ").append(groupBy);
        if (sort != null) builder.append(" ORDER BY ").append(sort);
        if (skip != null || limit != null) {
            if (dialect == Dialect.MYSQL) {
                builder.append(" LIMIT ?,?");
            } else if (dialect == Dialect.POSTGRESQL) {
                builder.append(" OFFSET ? LIMIT ?");
            }
        }
        return builder.toString();
    }

    Object[] params(List<Object> params, Integer skip, Integer limit) {
        if (skip != null && limit == null) throw new Error("limit must not be null if skip is not, skip=" + skip);
        if (skip == null && limit == null) return params.toArray();

        Integer skipValue = skip == null ? (Integer) 0 : skip;
        if (params.isEmpty()) return new Object[]{skipValue, limit};
        int length = params.size();
        @SuppressWarnings("PMD.OptimizableToArrayCall")     // false positive, to create array with larger size
        Object[] results = params.toArray(new Object[params.size() + 2]);
        results[length] = skipValue;
        results[length + 1] = limit;
        return results;
    }
}
