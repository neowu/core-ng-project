package core.framework.internal.db;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Supplier;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public class MySQLQueryInterceptor implements QueryInterceptor {
    // mysql will create new interceptor instance for every connection, so to minimize initialization cost
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLQueryInterceptor.class);

    @Override
    public QueryInterceptor init(MysqlConnection connection, Properties properties, Log log) {
        return this;
    }

    @Override
    public <T extends Resultset> T preProcess(Supplier<String> sql, Query interceptedQuery) {
        return null;
    }

    @Override
    public boolean executeTopLevelOnly() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public <T extends Resultset> T postProcess(Supplier<String> sql, Query interceptedQuery, T originalResultSet, ServerSession serverSession) {
        boolean noIndexUsed = serverSession.noIndexUsed();
        boolean badIndexUsed = serverSession.noGoodIndexUsed();
        if (noIndexUsed || badIndexUsed) {
            boolean suppress = suppressSlowSQLWarning();
            String message = noIndexUsed ? "no index used" : "bad index used";
            String sqlValue = sql.get();
            if (suppress) {
                LOGGER.debug("{}, sql={}", message, sqlValue);
            } else {
                LOGGER.warn(errorCode("SLOW_SQL"), "{}, sql={}", message, sqlValue);
            }
        }
        return null;
    }

    private boolean suppressSlowSQLWarning() {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            return actionLog.warningContext.suppressSlowSQLWarning;
        }
        return false;
    }
}
