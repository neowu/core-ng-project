package core.framework.internal.db.inspector;

import core.framework.util.ASCII;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.framework.log.Markers.errorCode;

public class QueryInspector {
    final Map<String, QueryState> states = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(QueryInspector.class);
    private final @Nullable QueryAnalyzer analyzer;

    public QueryInspector(@Nullable QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void inspect(String sql, Object[] params) {
        validateSQL(sql);

        if (analyzer == null) return; // HSQL doesn't have analyzer
        QueryState state = states.get(sql);

        if (state == null || state.inefficientAndStale()) { // re-inspect inefficient query every 24 hours
            states.put(sql, new QueryState(true, -1));  // placeholder to avoid repeated inspect and not slow down same concurrent queries

            QueryPlan plan = analyzer.explain(sql, params);
            if (!plan.efficient()) {
                // only log slow sql once for every 24 hours, and query plan may change over time due to db stats update
                logger.warn(errorCode("INEFFICIENT_QUERY"), "inefficient query, sql={}, plan=\n{}", sql, plan.plan());
                states.put(sql, new QueryState(false, System.currentTimeMillis()));
            }
        }
    }

    public String explain(String sql, Object[] params) {
        if (analyzer == null) return "";    // only unit test doesn't have analyzer
        QueryPlan plan = analyzer.explain(sql, params);
        return plan.plan();
    }

    void validateSQL(String sql) {
        if (sql.startsWith("CREATE ")) return;  // ignore DDL

        // validate asterisk
        // execute() could have select part, e.g. insert into select
        int index = sql.indexOf('*');
        while (index > -1) {   // check whether it's wildcard or multiply operator
            int length = sql.length();
            char ch = 0;
            index++;
            for (; index < length; index++) {
                ch = sql.charAt(index);
                if (ch != ' ') break;   // seek to next non-whitespace
            }
            if (ch == ','
                || index == length  // sql ends with *
                || index + 4 <= length && ASCII.toUpperCase(ch) == 'F' && "FROM".equals(ASCII.toUpperCase(sql.substring(index, index + 4))))
                throw new Error("sql must not contain wildcard(*), please only select columns needed, sql=" + sql);
            index = sql.indexOf('*', index + 1);
        }

        // validate string value
        // by this way, it also disallows functions with string values, e.g. IFNULL(column, 'value'), but it usually can be prevented by different design,
        // and we prefer to simplify db usage if possible, and shift complexity to application layer
        if (sql.indexOf('\'') != -1)
            throw new Error("sql must not contain single quote('), please use prepared statement and question mark(?), sql=" + sql);
    }

    record QueryState(boolean efficient, long timestamp) {
        boolean inefficientAndStale() {
            return !efficient && System.currentTimeMillis() - timestamp > 86_400_000;
        }
    }
}
