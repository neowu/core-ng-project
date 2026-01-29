package core.framework.internal.db.inspector;

import core.framework.internal.db.DatabaseOperation;
import core.framework.internal.db.ResultSetWrapper;
import core.framework.internal.db.RowMapper;

import java.sql.SQLException;
import java.util.List;

public class MySQLQueryAnalyzer implements QueryAnalyzer {
    private final DatabaseOperation operation;

    public MySQLQueryAnalyzer(DatabaseOperation operation) {
        this.operation = operation;
    }

    @Override
    public QueryPlan explain(String sql, Object[] params) {
        List<Explain> explains = operation.select("EXPLAIN " + sql, new ExplainRowMapper(), params);
        String plan = format(explains);
        for (Explain explain : explains) {
            if (!isEfficient(explain)) {
                return new QueryPlan(plan, false);
            }
        }
        return new QueryPlan(plan, true);
    }

    boolean isEfficient(Explain explain) {
        if (explain.table != null && explain.table.startsWith("<derived")) {
            return true; // skip derived table
        }
        if ("ALL".equals(explain.type) && explain.rowsGreaterThan(2000)) {
            return false;   // table scan more than 2000 rows is inefficient
        }
        if (explain.extraContains("Using filesort") && explain.rowsGreaterThan(50_000)) {
            return false;
        }
        // only allow Using temporary for range queries
        if (explain.extraContains("Using temporary") && !"range".equals(explain.type) && explain.rowsGreaterThan(50_000)) {
            return false;
        }
        return true;
    }

    String format(List<Explain> explains) {
        var builder = new StringBuilder(256);
        builder.append("id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra");
        for (Explain result : explains) {
            builder.append('\n')
                .append(result.id).append(" | ")
                .append(result.selectType).append(" | ")
                .append(result.table).append(" | ")
                .append(result.partitions).append(" | ")
                .append(result.type).append(" | ")
                .append(result.possibleKeys).append(" | ")
                .append(result.key).append(" | ")
                .append(result.keyLength).append(" | ")
                .append(result.ref).append(" | ")
                .append(result.rows).append(" | ")
                .append(result.filtered).append(" | ")
                .append(result.extra);
        }
        return builder.toString();
    }

    static class Explain {
        String id;
        String selectType;
        String table;
        String partitions;
        String type;
        String possibleKeys;
        String key;
        String keyLength;
        String ref;
        Long rows;
        String filtered;
        String extra;

        boolean rowsGreaterThan(long threshold) {
            return rows != null && rows > threshold;
        }

        boolean extraContains(String keyword) {
            return extra != null && extra.contains(keyword);
        }
    }

    static class ExplainRowMapper implements RowMapper<Explain> {
        @Override
        public Explain map(ResultSetWrapper resultSet) throws SQLException {
            var plan = new Explain();
            plan.id = resultSet.getString("id");
            plan.selectType = resultSet.getString("select_type");
            plan.table = resultSet.getString("table");
            plan.partitions = resultSet.getString("partitions");
            plan.type = resultSet.getString("type");
            plan.possibleKeys = resultSet.getString("possible_keys");
            plan.key = resultSet.getString("key");
            plan.keyLength = resultSet.getString("key_len");
            plan.ref = resultSet.getString("ref");
            plan.rows = resultSet.getLong("rows");
            plan.filtered = resultSet.getString("filtered");
            plan.extra = resultSet.getString("Extra");
            return plan;
        }
    }
}
