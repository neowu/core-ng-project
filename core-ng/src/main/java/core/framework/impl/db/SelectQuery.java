package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.db.dialect.Dialect;
import core.framework.impl.db.dialect.MySQLDialect;
import core.framework.impl.db.dialect.OracleDialect;
import core.framework.impl.reflect.Classes;
import core.framework.util.Exceptions;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author neo
 */
final class SelectQuery<T> {
    final String getSQL;
    final Dialect dialect;
    private final String selectSQL;
    private final String countSQL;

    SelectQuery(Class<?> entityClass, Vendor vendor) {
        String table = entityClass.getDeclaredAnnotation(Table.class).name();

        StringBuilder builder = new StringBuilder();
        List<Field> fields = Classes.instanceFields(entityClass);
        int index = 0;
        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            index++;
        }
        String columns = builder.toString();

        selectSQL = "SELECT " + columns + " FROM " + table;
        countSQL = "SELECT count(1) FROM " + table;

        builder = new StringBuilder(selectSQL).append(" WHERE ");
        index = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column column = field.getDeclaredAnnotation(Column.class);
                if (index > 0) builder.append(" AND ");
                builder.append(column.name()).append(" = ?");
                index++;
            }
        }
        getSQL = builder.toString();

        dialect = dialect(vendor, table, columns);
    }

    private Dialect dialect(Vendor vendor, String table, String columns) {
        switch (vendor) {
            case MYSQL:
                return new MySQLDialect(table, columns);
            case ORACLE:
                return new OracleDialect(table, columns);
            default:
                throw Exceptions.error("not supported vendor, vendor={}", vendor);
        }
    }

    String selectSQL(String where) {
        StringBuilder builder = new StringBuilder(selectSQL);
        if (where != null) builder.append(" WHERE ").append(where);
        return builder.toString();
    }

    String countSQL(String where) {
        StringBuilder builder = new StringBuilder(countSQL);
        if (where != null) builder.append(" WHERE ").append(where);
        return builder.toString();
    }
}
