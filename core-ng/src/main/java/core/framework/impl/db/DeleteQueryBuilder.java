package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;

/**
 * @author neo
 */
final class DeleteQueryBuilder {
    static String build(Class<?> entityClass) {
        Table table = entityClass.getDeclaredAnnotation(Table.class);
        var builder = new StringBuilder("DELETE FROM ").append(table.name()).append(" WHERE ");
        int index = 0;
        for (Field field : Classes.instanceFields(entityClass)) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) builder.append(" AND ");
                builder.append(column.name()).append(" = ?");
                index++;
            }
        }
        return builder.toString();
    }
}
