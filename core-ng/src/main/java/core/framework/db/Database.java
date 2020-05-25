package core.framework.db;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Database {
    /**
     * for expected slow sql, such as query with small table or db chooses different query plan,
     * disable warning then enable after<p>
     * e.g.
     * <blockquote><pre>
     * Database.enableSlowSQLWarning(false)
     * database.select(...)
     * Database.enableSlowSQLWarning(true)
     * </pre></blockquote>
     *
     * @param enable whether enable slow sql warning
     */
    static void enableSlowSQLWarning(boolean enable) {
        // pass flag thru thread local to MySQLQueryInterceptor, and put in action log to reset for every action
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.enableSlowSQLWarning = enable;
        }
    }

    <T> List<T> select(String sql, Class<T> viewClass, Object... params);

    <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params);

    int execute(String sql, Object... params);

    int[] batchExecute(String sql, List<Object[]> params);

    Transaction beginTransaction();
}
