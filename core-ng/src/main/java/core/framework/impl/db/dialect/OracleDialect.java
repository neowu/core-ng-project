package core.framework.impl.db.dialect;

import java.util.List;

/**
 * @author neo
 */
public class OracleDialect implements Dialect {
    private final String table;
    private final String columns;

    public OracleDialect(String table, String columns) {
        this.table = table;
        this.columns = columns;
    }

    @Override
    public String fetchSQL(String where, String sort, Integer skip, Integer limit) {
        if (skip == null && limit == null) {
            StringBuilder builder = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(table);
            if (where != null) builder.append(" WHERE ").append(where);
            if (sort != null) builder.append(" ORDER BY ").append(sort);
            return builder.toString();
        }

        if (sort == null) {
            StringBuilder builder = new StringBuilder(256)
                    .append("SELECT ").append(columns).append(" FROM (SELECT ROWNUM AS row_num, ").append(columns)
                    .append(" FROM ").append(table).append(" WHERE ");
            if (where != null) builder.append(where).append(" AND ");
            builder.append(" ROWNUM <= ?) WHERE row_num >= ?");
            return builder.toString();
        } else {
            StringBuilder builder = new StringBuilder(256)
                    .append("SELECT ").append(columns).append(" FROM (SELECT ROWNUM AS row_num, ").append(columns)
                    .append(" FROM (SELECT ").append(columns).append(" FROM ").append(table);
            if (where != null) builder.append(" WHERE ").append(where);
            builder.append(" ORDER BY ").append(sort).append(") WHERE ROWNUM <= ?) WHERE row_num >= ?");
            return builder.toString();
        }
    }

    @Override
    public Object[] fetchParams(List<Object> params, Integer skip, Integer limit) {
        int skipValue = skip == null ? 0 : skip;
        int fromRowNum = skipValue + 1;
        int toRowNum = skipValue + limit;
        if (params.isEmpty()) return new Object[]{toRowNum, fromRowNum};
        int length = params.size();
        Object[] results = params.toArray(new Object[params.size() + 2]);
        results[length] = toRowNum;
        results[length + 1] = fromRowNum;
        return results;
    }
}
