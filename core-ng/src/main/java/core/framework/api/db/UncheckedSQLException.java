package core.framework.api.db;

import java.sql.SQLException;

/**
 * @author neo
 */
public class UncheckedSQLException extends RuntimeException {
    private static final long serialVersionUID = 5857178985477320780L;

    public UncheckedSQLException(SQLException cause) {
        super(cause);
    }
}
