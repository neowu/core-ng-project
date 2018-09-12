package core.framework.impl.db.dialect;

import java.util.List;

/**
 * @author neo
 */
public class MySQLDialect implements Dialect {
    private final String table;
    private final String columns;

    public MySQLDialect(String table, String columns) {
        this.table = table;
        this.columns = columns;
    }

    @Override
    public String fetchSQL(String where, String sort, Integer skip, Integer limit) {
        var builder = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(table);
        if (where != null) builder.append(" WHERE ").append(where);
        if (sort != null) builder.append(" ORDER BY ").append(sort);
        if (skip != null || limit != null) builder.append(" LIMIT ?,?");
        return builder.toString();
    }

    @Override
    public Object[] fetchParams(List<Object> params, Integer skip, Integer limit) {
        Integer skipValue = skip == null ? Integer.valueOf(0) : skip;
        if (params.isEmpty()) return new Object[]{skipValue, limit};
        int length = params.size();
        Object[] results = params.toArray(new Object[params.size() + 2]);
        results[length] = skipValue;
        results[length + 1] = limit;
        return results;
    }
}
