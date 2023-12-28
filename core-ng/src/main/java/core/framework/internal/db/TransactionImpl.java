package core.framework.internal.db;


import core.framework.db.Transaction;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
final class TransactionImpl implements Transaction {
    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private final TransactionManager transactionManager;
    private final StopWatch watch = new StopWatch();
    private final long longTransactionThresholdInNanos;

    TransactionImpl(TransactionManager transactionManager, long longTransactionThresholdInNanos) {
        logger.debug("begin transaction");
        this.transactionManager = transactionManager;
        this.longTransactionThresholdInNanos = longTransactionThresholdInNanos;
    }

    @Override
    public void rollback() {
        var watch = new StopWatch();
        try {
            transactionManager.rollbackTransaction();
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("rollback transaction, elapsed={}", elapsed);
            ActionLogContext.track("db", elapsed);
        }
    }

    @Override
    public void commit() {
        var watch = new StopWatch();
        try {
            transactionManager.commitTransaction();
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("commit transaction, elapsed={}", elapsed);
            ActionLogContext.track("db", elapsed);
        }
    }

    @Override
    public void close() {
        try {
            transactionManager.endTransaction();
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("end transaction, elapsed={}", elapsed);
            if (elapsed > longTransactionThresholdInNanos) {
                logger.warn(Markers.errorCode("LONG_TRANSACTION"), "long db transaction, elapsed={}", Duration.ofNanos(elapsed));
            }
        }
    }
}
