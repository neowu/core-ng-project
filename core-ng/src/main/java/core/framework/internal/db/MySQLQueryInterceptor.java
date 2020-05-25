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
    private final Logger logger = LoggerFactory.getLogger(MySQLQueryInterceptor.class);

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
            boolean warning = isSlowSQLWarningEnabled();
            String message = noIndexUsed ? "no index used" : "bad index used";
            String sqlValue = sql.get();
            if (warning) {
                logger.warn(errorCode("SLOW_SQL"), "{}, sql={}", message, sqlValue);
            } else {
                logger.debug("{}, sql={}", message, sqlValue);
            }
        }
        return null;
    }

    private boolean isSlowSQLWarningEnabled() {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            return actionLog.enableSlowSQLWarning;
        }
        return true;
    }
}
