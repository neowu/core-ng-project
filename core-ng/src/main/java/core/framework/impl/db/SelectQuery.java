package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Query;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author neo
 */
final class SelectQuery {
    final String selectByPrimaryKeys;
    final String select;

    SelectQuery(Class<?> entityClass) {
        StringBuilder builder = new StringBuilder("SELECT ");
        Field[] fields = entityClass.getFields();
        int index = 0;
        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            index++;
        }

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(" FROM ").append(table.name());

        select = builder.toString();

        builder.append(" WHERE ");
        index = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) builder.append(" AND ");
                builder.append(column.name()).append(" = ?");
                index++;
            }
        }

        selectByPrimaryKeys = builder.toString();
    }

    String sql(String where, Integer skip, Integer limit) {
        StringBuilder builder = new StringBuilder(select);
        if (where != null) builder.append(" WHERE ").append(where);
        if (skip != null || limit != null) builder.append(" LIMIT ?,?");
        return builder.toString();
    }

    Object[] params(Query query) {
        if (query.skip != null && query.limit == null) throw Exceptions.error("limit must not be null if skip is not, skip={}", query.skip);
        if (query.skip == null && query.limit == null) return query.params;

        Integer skip = query.skip == null ? Integer.valueOf(0) : query.skip;
        if (query.params == null) return new Object[]{skip, query.limit};
        int length = query.params.length;
        Object[] params = Arrays.copyOf(query.params, length + 2);
        params[length] = skip;
        params[length + 1] = query.limit;
        return params;
    }
}
