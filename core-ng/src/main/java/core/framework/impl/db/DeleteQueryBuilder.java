package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public class DeleteQueryBuilder {
    public final String sql;

    DeleteQueryBuilder(Class<?> entityClass) {
        Table table = entityClass.getDeclaredAnnotation(Table.class);
        StringBuilder builder = new StringBuilder("DELETE FROM ")
            .append(table.name())
            .append(" WHERE ");
        Field[] fields = entityClass.getFields();
        int index = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) builder.append(" AND ");
                builder.append(column.name()).append("=?");
                index++;
            }
        }

        sql = builder.toString();
    }
}
