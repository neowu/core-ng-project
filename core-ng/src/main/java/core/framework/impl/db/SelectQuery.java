package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;

import java.lang.reflect.Field;

/**
 * @author neo
 */
final class SelectQuery {
    final String selectByPrimaryKeys;
    final String selectAll;

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

        selectAll = builder.toString();

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

    String byWhere(String whereClause) {
        return selectAll + " WHERE " + whereClause;
    }
}
