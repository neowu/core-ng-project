package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Query;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public class SelectQueryBuilder {
    private final String selectByPK;
    private final String selectAll;

    SelectQueryBuilder(Class<?> entityClass) {
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

        selectByPK = builder.toString();
    }

    public Query byPK(Object... primaryKeys) {
        Query query = new Query(selectByPK);
        for (Object primaryKey : primaryKeys) {
            query.addParam(primaryKey);
        }
        return query;
    }

    public Query all() {
        return new Query(selectAll);
    }

    public Query where(String whereClause, Object... params) {
        if (!whereClause.contains("?"))
            throw Exceptions.error("where clause must contain parameter holder, whereClause={}", whereClause);

        Query query = new Query(selectAll).appendStatement(" WHERE ").appendStatement(whereClause);
        for (Object primaryKey : params) {
            query.addParam(primaryKey);
        }
        return query;
    }
}
