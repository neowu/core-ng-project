package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Lists;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author neo
 */
final class InsertQueryBuilder {
    public final String sql;
    private final Field[] paramFields;

    InsertQueryBuilder(Class<?> entityClass) {
        List<Field> paramFields = Lists.newArrayList();

        StringBuilder builder = new StringBuilder("INSERT INTO ");

        Table table = entityClass.getDeclaredAnnotation(Table.class);
        builder.append(table.name()).append(" (");

        Field[] fields = entityClass.getFields();

        int index = 0;
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            if (primaryKey != null && primaryKey.autoIncrement()) continue;

            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            paramFields.add(field);
            index++;
        }

        builder.append(") VALUES (");
        for (int i = 0; i < paramFields.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append('?');
        }
        builder.append(')');

        sql = builder.toString();
        this.paramFields = paramFields.toArray(new Field[paramFields.size()]);
    }

    Object[] params(Object entity) {
        Object[] params = new Object[paramFields.length];
        try {
            for (int i = 0; i < paramFields.length; i++) {
                Field field = paramFields[i];
                params[i] = field.get(entity);
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
        return params;
    }
}
