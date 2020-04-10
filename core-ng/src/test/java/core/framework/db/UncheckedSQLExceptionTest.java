package core.framework.db;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class UncheckedSQLExceptionTest {
    @Test
    void errorType() {
        var exception = new UncheckedSQLException(new SQLException("connection abort", "08S01", 1152));
        assertThat(exception.errorType).isEqualTo(UncheckedSQLException.ErrorType.CONNECTION_ERROR);
    }
}
