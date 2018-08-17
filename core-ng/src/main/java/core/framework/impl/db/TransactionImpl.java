package core.framework.impl.db;


import core.framework.db.Transaction;
import core.framework.log.Markers;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        transactionManager.rollbackTransaction();
    }

    @Override
    public void commit() {
        transactionManager.commitTransaction();
    }

    @Override
    public void close() {
        try {
            transactionManager.endTransaction();
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("end transaction, elapsed={}", elapsed);
            if (elapsed > longTransactionThresholdInNanos) {
                logger.warn(Markers.errorCode("LONG_TRANSACTION"), "long db transaction, elapsed={}", elapsed);
            }
        }
    }
}
