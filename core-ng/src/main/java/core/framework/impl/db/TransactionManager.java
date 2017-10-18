package core.framework.impl.db;

import core.framework.db.IsolationLevel;
import core.framework.db.Transaction;
import core.framework.db.UncheckedSQLException;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

/**
 * @author neo
 */
public final class TransactionManager {
    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final ThreadLocal<PoolItem<Connection>> currentConnection = new ThreadLocal<>();
    private final ThreadLocal<TransactionState> currentTransactionState = new ThreadLocal<>();
    private final Pool<Connection> pool;
    public IsolationLevel defaultIsolationLevel;
    public long longTransactionThresholdInNanos = Duration.ofSeconds(10).toNanos();

    TransactionManager(Pool<Connection> pool) {
        this.pool = pool;
    }

    PoolItem<Connection> getConnection() {
        PoolItem<Connection> connection = currentConnection.get();
        if (connection != null) {
            TransactionState state = currentTransactionState.get();
            if (state != TransactionState.START)
                throw Exceptions.error("db access is not allowed after transaction ended, currentState={}", state);
            return connection;
        }

        return getConnectionFromPool();
    }

    void returnConnection(PoolItem<Connection> connection) {
        if (currentConnection.get() == null)
            returnConnectionToPool(connection);
    }

    Transaction beginTransaction() {
        if (currentConnection.get() != null)
            throw new Error("nested transaction is not supported, please contact arch team");

        PoolItem<Connection> connection = getConnectionFromPool();
        try {
            connection.resource.setAutoCommit(false);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }

        // set state after real operation done, to avoid ending up unexpected state if error occurs
        currentTransactionState.set(TransactionState.START);
        currentConnection.set(connection);

        return new TransactionImpl(this, longTransactionThresholdInNanos);
    }

    void commitTransaction() {
        PoolItem<Connection> connection = currentConnection.get();
        try {
            logger.debug("commit transaction");
            connection.resource.commit();
            currentTransactionState.set(TransactionState.COMMITTED);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }
    }

    void rollbackTransaction() {
        PoolItem<Connection> connection = currentConnection.get();
        try {
            logger.debug("rollback transaction");
            connection.resource.rollback();
            currentTransactionState.set(TransactionState.ROLLED_BACK);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }
    }

    void endTransaction() {
        PoolItem<Connection> connection = currentConnection.get();
        TransactionState state = currentTransactionState.get();
        // clean up state first, to avoid ending up with unexpected state
        currentConnection.remove();
        currentTransactionState.remove();

        try {
            if (state == TransactionState.START) {
                logger.warn("roll back transaction due to state not changed, it could be either transaction.commit() not executed or exception occurred");
                connection.resource.rollback();
            }
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            returnConnectionToPool(connection);
        }
    }

    private PoolItem<Connection> getConnectionFromPool() {
        PoolItem<Connection> connection = pool.borrowItem();
        try {
            if (defaultIsolationLevel != null)
                connection.resource.setTransactionIsolation(defaultIsolationLevel.level);
            return connection;
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            pool.returnItem(connection);
            throw new UncheckedSQLException(e);
        }
    }

    private void returnConnectionToPool(PoolItem<Connection> connection) {
        try {
            if (!connection.broken)
                connection.resource.setAutoCommit(true);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            pool.returnItem(connection);
        }
    }

    private enum TransactionState {
        START, COMMITTED, ROLLED_BACK
    }
}
