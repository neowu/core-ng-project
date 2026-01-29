package core.framework.internal.db.inspector;

import core.framework.internal.db.inspector.QueryAnalyzer.QueryPlan;
import core.framework.util.ASCII;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.framework.log.Markers.errorCode;

public class QueryInspector {
    private final Logger logger = LoggerFactory.getLogger(QueryInspector.class);
    private final Map<String, Long> lastCheckTimestamps = new ConcurrentHashMap<>();
    private final @Nullable QueryAnalyzer analyzer;

    public QueryInspector(@Nullable QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void explain(String sql, Object[] params, boolean force) {
        if (analyzer == null) return;    // only unit tests don't have analyzer

        if (!force) {
            Long timestamp = lastCheckTimestamps.get(sql);
            long now = System.currentTimeMillis();
            if (timestamp != null && now - timestamp < 21_600_000) return; // check every 6 hours
            lastCheckTimestamps.put(sql, now);        // to avoid duplicate analysis as much as possible
        }

        QueryPlan plan = analyzer.explain(sql, params);
        if (plan != null) {
            if (!plan.efficient()) {
                logger.warn(errorCode("INEFFICIENT_QUERY"), "inefficient query, plan:\n{}", plan.plan());
            } else if (force) {
                logger.debug("plan:\n{}", plan.plan());
            }
        }
    }

    public void validateSQL(String sql) {
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
}
