package core.framework.internal.db.inspector;

import core.framework.internal.db.DatabaseOperation;
import core.framework.internal.db.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PostgreSQLQueryAnalyzer implements QueryAnalyzer {
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLQueryAnalyzer.class);
    private final DatabaseOperation operation;

    public PostgreSQLQueryAnalyzer(DatabaseOperation operation) {
        this.operation = operation;
    }

    @Override
    public QueryPlan explain(String sql, Object[] params) {
        List<String> explains = operation.select("EXPLAIN " + sql, new RowMapper.StringRowMapper(), params);
        String plan = String.join("\n", explains);
        for (String explain : explains) {
            if (!isEfficient(explain)) {
                return new QueryPlan(plan, false);
            }
        }
        return new QueryPlan(plan, true);
    }

    // Seq Scan on some_table (cost=0.00..657.94 rows=976 width=96)
    boolean isEfficient(String queryPlan) {
        if (queryPlan.contains("Seq Scan")) {
            long rows = parseRows(queryPlan);
            return rows <= 2000; // accept seq scan for small table
        } else if (queryPlan.contains("Hash") && queryPlan.contains("Join")) {
            // hash join may load large table into memory, consider inefficient
            long rows = parseRows(queryPlan);
            return rows <= 50_000;
        }
        return true;
    }

    private long parseRows(String queryPlan) {
        int rowsIndex = queryPlan.indexOf(" rows=");
        if (rowsIndex < 0) return -1;
        int spaceIndex = queryPlan.indexOf(' ', rowsIndex + 5);
        if (spaceIndex < 0) return -1;
        try {
            return Long.parseLong(queryPlan.substring(rowsIndex + 6, spaceIndex));
        } catch (NumberFormatException e) {
            logger.warn("failed to parse query plan, plan={}", queryPlan, e);
            return -1;
        }
    }
}
