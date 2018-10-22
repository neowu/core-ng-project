package core.framework.impl.db;

import core.framework.impl.resource.PoolItem;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ConnectionsTest {
    @Test
    void withSQLConnectionError() {
        // refer to https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-error-sqlstates.html
        PoolItem<Connection> connection = new PoolItem<>(null);
        Connections.checkConnectionState(connection, new SQLException("ER_HANDSHAKE_ERROR", "08S01"));
        assertThat(connection.broken).isTrue();
    }

    @Test
    void withQueryTimeout() {
        PoolItem<Connection> connection = new PoolItem<>(null);
        Connections.checkConnectionState(connection, new SQLTimeoutException("Statement cancelled due to timeout or client request"));
        assertThat(connection.broken).isTrue();
    }
}
