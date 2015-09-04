package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author neo
 */
final class UpdateQueryBuilder {
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

    Query query(Object entity) {
        try {
            StringBuilder sql = new StringBuilder("UPDATE ");
            List<Object> params = Lists.newArrayList();

            Class<?> entityClass = entity.getClass();

            Table table = entityClass.getDeclaredAnnotation(Table.class);
            sql.append(table.name())
                .append(" SET ");

            int index = 0;
            for (Field field : columnFields) {
                Object value = field.get(entity);
                if (value != null) {
                    Column column = field.getDeclaredAnnotation(Column.class);
                    if (index > 0) sql.append(", ");
                    sql.append(column.name()).append(" = ?");
                    params.add(value);
                    index++;
                }
            }

            if (index == 0) throw new Error("all fields are null");

            sql.append(" WHERE ");
            index = 0;
            for (Field field : primaryKeyFields) {
                Object value = field.get(entity);
                if (value == null) throw Exceptions.error("primary key can not be null, field={}", field);
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) sql.append(" AND ");
                sql.append(column.name()).append(" = ?");
                params.add(value);
                index++;
            }

            return new Query(sql.toString(), params.toArray());
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    static class Query {
        final String sql;
        final Object[] params;

        Query(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
