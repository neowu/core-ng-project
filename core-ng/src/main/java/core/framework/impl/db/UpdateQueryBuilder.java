package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Query;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author neo
 */
public class UpdateQueryBuilder {
    private final List<Field> primaryKeyFields = Lists.newArrayList();
    private final List<Field> columnFields = Lists.newArrayList();

    UpdateQueryBuilder(Class entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                primaryKeyFields.add(field);
            } else {
                columnFields.add(field);
            }
        }
    }

    public Query query(Object entity) {
        try {
            Query query = new Query("UPDATE ");

            Class<?> entityClass = entity.getClass();

            Table table = entityClass.getDeclaredAnnotation(Table.class);
            query.appendStatement(table.name())
                .appendStatement(" SET ");

            int index = 0;
            for (Field field : columnFields) {
                Object value = field.get(entity);
                if (value != null) {
                    Column column = field.getDeclaredAnnotation(Column.class);
                    if (index > 0) query.appendStatement(", ");
                    query.appendStatement(column.name()).appendStatement(" = ?");
                    query.addParam(value);
                    index++;
                }
            }

            if (index == 0) throw new Error("all fields are null");

            query.appendStatement(" WHERE ");
            index = 0;
            for (Field field : primaryKeyFields) {
                Object value = field.get(entity);
                if (value == null) throw Exceptions.error("primary key can not be null, field={}", field);
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) query.appendStatement(" AND ");
                query.appendStatement(column.name()).appendStatement(" = ?");
                query.addParam(value);
                index++;
            }

            return query;
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
