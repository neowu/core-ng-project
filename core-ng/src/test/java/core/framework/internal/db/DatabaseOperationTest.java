package core.framework.internal.db;

import core.framework.db.Database;
import core.framework.db.QueryDiagnostic;
import core.framework.internal.log.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseOperationTest {
    @Mock(extraInterfaces = QueryDiagnostic.class)
    PreparedStatement statement;
    private DatabaseOperation operation;
    private LogManager logManager;

    @BeforeEach
    void createDatabaseOperation() {
        operation = new DatabaseOperation(null);
        logManager = new LogManager();
    }

    @Test
    void logSlowQuery() {
        QueryDiagnostic diagnostic = (QueryDiagnostic) statement;
        when(diagnostic.sql()).thenReturn("sql");
        when(diagnostic.noIndexUsed()).thenReturn(Boolean.FALSE);
        when(diagnostic.noGoodIndexUsed()).thenReturn(Boolean.TRUE);

        logManager.run("test", null, actionLog -> {
            operation.logSlowQuery(statement);
            return null;
        });
    }

    @Test
    void suppressSlowSQLWarning() {
        QueryDiagnostic diagnostic = (QueryDiagnostic) statement;
        when(diagnostic.sql()).thenReturn("sql");
        when(diagnostic.noIndexUsed()).thenReturn(Boolean.TRUE);
        when(diagnostic.noGoodIndexUsed()).thenReturn(Boolean.FALSE);

        logManager.run("test", null, actionLog -> {
            Database.suppressSlowSQLWarning(true);
            operation.logSlowQuery(statement);
            Database.suppressSlowSQLWarning(false);
            assertThat(actionLog.errorCode()).isNull();

            operation.logSlowQuery(statement);
            assertThat(actionLog.errorCode()).isEqualTo("SLOW_SQL");
            return null;
        });
    }
}
