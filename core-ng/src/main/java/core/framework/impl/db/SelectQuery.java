package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.db.dialect.Dialect;
import core.framework.impl.db.dialect.MySQLDialect;
import core.framework.impl.db.dialect.OracleDialect;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;
import java.util.List;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class SelectQuery<T> {
    final String getSQL;
    private final Dialect dialect;
    private final String table;

    SelectQuery(Class<?> entityClass, Vendor vendor) {
        table = entityClass.getDeclaredAnnotation(Table.class).name();

        List<Field> fields = Classes.instanceFields(entityClass);
        String columns = columns(fields);

        StringBuilder builder = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(table).append(" WHERE ");
        int index = 0;
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

    private String columns(List<Field> fields) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (index > 0) builder.append(", ");
            builder.append(column.name());
            index++;
        }
        return builder.toString();
    }

    private Dialect dialect(Vendor vendor, String table, String columns) {
        switch (vendor) {
            case MYSQL:
                return new MySQLDialect(table, columns);
            case ORACLE:
                return new OracleDialect(table, columns);
            default:
                throw new Error(format("not supported vendor, vendor={}", vendor));
        }
    }

    String projectionSQL(String projection, StringBuilder where) {
        StringBuilder builder = new StringBuilder("SELECT ").append(projection).append(" FROM ").append(table);
        if (where.length() > 0) builder.append(" WHERE ").append(where);
        return builder.toString();
    }

    String fetchSQL(StringBuilder where, String sort, Integer skip, Integer limit) {
        return dialect.fetchSQL(where.length() > 0 ? where.toString() : null, sort, skip, limit);
    }

    Object[] fetchParams(List<Object> params, Integer skip, Integer limit) {
        if (skip != null && limit == null) throw new Error(format("limit must not be null if skip is not, skip={}", skip));
        if (skip == null && limit == null) return params.toArray();
        return dialect.fetchParams(params, skip, limit);
    }
}
