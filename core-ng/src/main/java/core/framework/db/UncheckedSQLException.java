package core.framework.db;

import java.sql.SQLException;

/**
 * @author neo
 */
public final class UncheckedSQLException extends RuntimeException {
    private static final long serialVersionUID = 5857178985477320780L;

    public UncheckedSQLException(SQLException e) {
        super(e.getMessage() + ", sqlState=" + e.getSQLState() + ", errorCode=" + e.getErrorCode(), e);
    }
}
