package core.framework.impl.db;


import core.framework.api.db.Transaction;
import core.framework.api.log.Markers;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
final class TransactionImpl implements Transaction {
    private final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private final TransactionManager transactionManager;
    private final StopWatch watch = new StopWatch();
    private final long longTransactionThresholdInMs;

    TransactionImpl(TransactionManager transactionManager, long longTransactionThresholdInMs) {
        logger.debug("begin transaction");
        this.transactionManager = transactionManager;
        this.longTransactionThresholdInMs = longTransactionThresholdInMs;
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
            long elapsedTime = watch.elapsedTime();
            logger.debug("end transaction, elapsedTime={}", elapsedTime);
            if (elapsedTime > longTransactionThresholdInMs) {
                logger.warn(Markers.errorCode("LONG_TRANSACTION"), "long db transaction, elapsedTime={}", elapsedTime);
            }
        }
    }
}
