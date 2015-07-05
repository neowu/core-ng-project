package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Lists;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class InsertQueryBuilder {
    public final String sql;
    private final List<Field> params = Lists.newArrayList();

    InsertQueryBuilder(Class<?> entityClass) {
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
            params.add(field);
            index++;
        }

        builder.append(") VALUES (");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append('?');
        }
        builder.append(')');

        sql = builder.toString();
    }

    public List<Object> params(Object entity) {
        List<Object> queryParams = new ArrayList<>(this.params.size());
        try {
            for (Field param : params) {
                queryParams.add(param.get(entity));
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
        return queryParams;
    }
}
