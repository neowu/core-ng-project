package core.framework.db;

import org.junit.jupiter.api.Test;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class UncheckedSQLExceptionTest {
    @Test
    void errorType() {
        var exception = new UncheckedSQLException(new SQLException("connection abort", "08S01", 1152));
        assertThat(exception.errorType).isEqualTo(UncheckedSQLException.ErrorType.CONNECTION_ERROR);

        exception = new UncheckedSQLException(new SQLException("No operations allowed after statement closed.", "S1009", 0));
        assertThat(exception.errorType).isEqualTo(UncheckedSQLException.ErrorType.CONNECTION_ERROR);

        // mimic exception case of mysql, refer to com.mysql.cj.jdbc.exceptions.SQLError.createBatchUpdateException
        var cause = new SQLIntegrityConstraintViolationException("Duplicate entry", "23000", 1062);
        exception = new UncheckedSQLException(new BatchUpdateException(cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), new int[0], cause));
        assertThat(exception.errorType).isEqualTo(UncheckedSQLException.ErrorType.INTEGRITY_CONSTRAINT_VIOLATION);
    }
}
