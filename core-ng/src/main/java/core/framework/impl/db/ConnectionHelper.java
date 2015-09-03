package core.framework.impl.db;

import core.framework.impl.resource.PoolItem;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author neo
 */
final class ConnectionHelper {
    // refer to com.mysql.jdbc.integration.c3p0.MysqlConnectionTester, and standard sql state list:
    // http://dev.mysql.com/doc/connector-j/en/connector-j-reference-error-sqlstates.html
    static void checkConnectionStatus(PoolItem<Connection> connection, SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("08")) {
            connection.broken = true;
        }
    }
}
