package core.framework.impl.db;

import core.framework.impl.resource.PoolItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * @author neo
 */
final class Connections {
    // refer to com.mysql.jdbc.integration.c3p0.MysqlConnectionTester, and standard sql state list:
    // http://dev.mysql.com/doc/connector-j/en/connector-j-reference-error-sqlstates.html
    // https://docs.oracle.com/cd/E16338_01/appdev.112/e10827/appd.htm#g642406
    static void checkConnectionState(PoolItem<Connection> connection, SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("08")) {
            connection.broken = true;
        }

        // if query timeout, com.mysql.cj.CancelQueryTaskImpl sends "KILL QUERY" to mysql server in cancel task thread
        // and as result, in current thread it triggers com.mysql.cj.jdbc.ConnectionImpl.handleCleanup with whyCleanedUp = com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure, The last packet successfully received from the server was x milliseconds ago.
        // so for query timeout, the connection needs to be marked as broken and close,
        // otherwise when other thread retrieves this connection, will encounter "statement is already closed" exception
        if (e instanceof SQLTimeoutException) {
            connection.broken = true;
        }
    }
}
