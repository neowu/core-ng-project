package core.framework.impl.db;

import core.framework.impl.resource.PoolItem;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ConnectionsTest {
    @Test
    void withSQLConnectionError() {
        // refer to https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-error-sqlstates.html
        PoolItem<Connection> connection = new PoolItem<>(null);
        Connections.checkConnectionStatus(connection, new SQLException("ER_HANDSHAKE_ERROR", "08S01"));
        assertTrue(connection.broken);
    }
}
