package core.framework.internal.db;

import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;
import core.framework.db.Database;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class MySQLQueryInterceptorTest {
    @Mock
    ServerSession session;
    private MySQLQueryInterceptor interceptor;
    private LogManager logManager;
    private ActionLog actionLog;

    @BeforeEach
    void createMySQLQueryInterceptor() {
        logManager = new LogManager();
        interceptor = new MySQLQueryInterceptor();

        actionLog = logManager.begin("begin", null);
    }

    @AfterEach
    void cleanup() {
        logManager.end("end");
    }

    @Test
    void defaultBehavior() {
        assertThat(interceptor.init(null, null, null)).isSameAs(interceptor);
        Resultset result = interceptor.preProcess(null, null);
        assertThat(result).isNull();
        assertThat(interceptor.executeTopLevelOnly()).isTrue();
        interceptor.destroy();
    }

    @Test
    void postProcess() {
        when(session.noGoodIndexUsed()).thenReturn(Boolean.TRUE);

        Database.suppressSlowSQLWarning(true);
        interceptor.postProcess(() -> "sql", null, null, session);
        Database.suppressSlowSQLWarning(false);
        assertThat(actionLog.errorCode()).isNull();

        interceptor.postProcess(() -> "sql", null, null, session);
        assertThat(actionLog.errorCode()).isEqualTo("SLOW_SQL");
    }
}
