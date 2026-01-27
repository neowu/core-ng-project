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
            if ("ALL".equals(explain.type) && explain.rows != null && explain.rows > 2000) {
                return new QueryPlan(plan, false);
            }
        }

        return new QueryPlan(plan, true);
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
        Long keyLength;
        String ref;
        Long rows;
        String filtered;
        String extra;
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
            plan.keyLength = resultSet.getLong("key_len");
            plan.ref = resultSet.getString("ref");
            plan.rows = resultSet.getLong("rows");
            plan.filtered = resultSet.getString("filtered");
            plan.extra = resultSet.getString("Extra");
            return plan;
        }
    }
}
