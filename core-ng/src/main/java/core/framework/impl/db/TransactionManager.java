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
    private static final ThreadLocal<PoolItem<Connection>> CURRENT_CONNECTION = new ThreadLocal<>();
    private static final ThreadLocal<TransactionState> CURRENT_TRANSACTION_STATE = new ThreadLocal<>();

    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final Pool<Connection> pool;
    public IsolationLevel defaultIsolationLevel;
    public long longTransactionThresholdInNanos = Duration.ofSeconds(10).toNanos();

    TransactionManager(Pool<Connection> pool) {
        this.pool = pool;
    }

    PoolItem<Connection> getConnection() {
        PoolItem<Connection> connection = CURRENT_CONNECTION.get();
        if (connection != null) {
            TransactionState state = CURRENT_TRANSACTION_STATE.get();
            if (state != TransactionState.START)
                throw Exceptions.error("db access is not allowed after transaction ended, currentState={}", state);
            return connection;
        }

        return getConnectionFromPool();
    }

    void returnConnection(PoolItem<Connection> connection) {
        if (CURRENT_CONNECTION.get() == null)
            returnConnectionToPool(connection);
    }

    Transaction beginTransaction() {
        if (CURRENT_CONNECTION.get() != null)
            throw new Error("nested transaction is not supported, please contact arch team");

        PoolItem<Connection> connection = getConnectionFromPool();
        try {
            connection.resource.setAutoCommit(false);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }

        // set state after real operation done, to avoid ending up unexpected state if error occurs
        CURRENT_TRANSACTION_STATE.set(TransactionState.START);
        CURRENT_CONNECTION.set(connection);

        return new TransactionImpl(this, longTransactionThresholdInNanos);
    }

    void commitTransaction() {
        PoolItem<Connection> connection = CURRENT_CONNECTION.get();
        try {
            logger.debug("commit transaction");
            connection.resource.commit();
            CURRENT_TRANSACTION_STATE.set(TransactionState.COMMIT);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }
    }

    void rollbackTransaction() {
        PoolItem<Connection> connection = CURRENT_CONNECTION.get();
        try {
            logger.debug("rollback transaction");
            connection.resource.rollback();
            CURRENT_TRANSACTION_STATE.set(TransactionState.ROLLBACK);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        }
    }

    void endTransaction() {
        PoolItem<Connection> connection = CURRENT_CONNECTION.get();
        TransactionState state = CURRENT_TRANSACTION_STATE.get();
        // clean up state first, to avoid ending up with unexpected state
        CURRENT_CONNECTION.remove();
        CURRENT_TRANSACTION_STATE.remove();

        try {
            if (state == TransactionState.START) {
                logger.warn("rollback transaction due to either transaction.commit() was not called or exception occurred");
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
        START, COMMIT, ROLLBACK
    }
}
