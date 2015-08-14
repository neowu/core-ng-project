package core.framework.impl.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import core.framework.api.db.IsolationLevel;
import core.framework.api.db.Transaction;
import core.framework.api.db.UncheckedSQLException;
import core.framework.api.util.Exceptions;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

/**
 * @author neo
 */
public class TransactionManager {
    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private final ThreadLocal<TransactionState> currentTransactionState = new ThreadLocal<>();
    private final ComboPooledDataSource dataSource;
    public IsolationLevel defaultIsolationLevel;
    public long longTransactionThresholdInMs = Duration.ofSeconds(10).toMillis();

    public TransactionManager(ComboPooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        Connection connection = currentConnection.get();
        if (connection != null) {
            TransactionState state = currentTransactionState.get();
            if (state != TransactionState.START)
                throw Exceptions.error("db access is not allowed after transaction ends, currentState={}", state);
            return connection;
        }

        return getConnectionFromPool();
    }

    public void releaseConnection(Connection connection) {
        if (currentConnection.get() == null)
            closeConnection(connection);
    }

    private Connection getConnectionFromPool() {
        Connection connection;
        StopWatch watch = new StopWatch();
        int available = -1;
        int total = -1;
        try {
            available = dataSource.getNumIdleConnections();
            total = dataSource.getNumConnections();
            connection = dataSource.getConnection();
            if (defaultIsolationLevel != null) connection.setTransactionIsolation(defaultIsolationLevel.level);
            return connection;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            logger.debug("get connection from pool, available={}, total={}, elapsedTime={}",
                available,
                total,
                watch.elapsedTime());
        }
    }

    public Transaction beginTransaction() {
        if (currentConnection.get() != null)
            throw new Error("nested transaction is not supported, please contact arch team");

        Connection connection = getConnectionFromPool();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }

        // set state after real operation done, to avoid ending up unexpected state if error occurs
        currentTransactionState.set(TransactionState.START);
        currentConnection.set(connection);

        return new TransactionImpl(this, longTransactionThresholdInMs);
    }

    public void commitTransaction() {
        try {
            logger.debug("commit transaction");
            currentConnection.get().commit();
            currentTransactionState.set(TransactionState.COMMITTED);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            logger.debug("rollback transaction");
            currentConnection.get().rollback();
            currentTransactionState.set(TransactionState.ROLLED_BACK);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void endTransaction() {
        Connection connection = currentConnection.get();
        TransactionState state = currentTransactionState.get();
        // clean up state first, to avoid ending up with unexpected state
        currentConnection.remove();
        currentTransactionState.remove();

        try {
            if (state == TransactionState.START) {
                logger.warn("roll back transaction due to state not changed, it could be either transaction.commit() not executed or exception occurred");
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private enum TransactionState {
        START, COMMITTED, ROLLED_BACK
    }
}
