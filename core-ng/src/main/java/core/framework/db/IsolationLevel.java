package core.framework.db;

import java.sql.Connection;

/**
 * @author neo
 */
public enum IsolationLevel {
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED), READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED);

    public final int level;

    IsolationLevel(int level) {
        this.level = level;
    }
}
