package com.mysql.cj;

import com.mysql.cj.Query.CancelStatus;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import core.framework.internal.db.DatabaseImpl;
import core.framework.internal.db.cloud.CloudAuthProvider;
import core.framework.internal.db.cloud.GCloudAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.TimerTask;

// track change with https://github.com/mysql/mysql-connector-j/blob/release/8.0/src/main/core-impl/java/com/mysql/cj/CancelQueryTaskImpl.java (last updated on 2022-01-17)
public class CancelQueryTaskImpl extends TimerTask implements CancelQueryTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelQueryTaskImpl.class);

    Query queryToCancel;
    Throwable caughtWhileCancelling = null;

    public CancelQueryTaskImpl(Query cancellee) {
        this.queryToCancel = cancellee;
    }

    @Override
    public boolean cancel() {
        boolean res = super.cancel();
        this.queryToCancel = null;
        return res;
    }

    @Override
    public void run() {
        Thread.ofVirtual().start(() -> {
            Query query = queryToCancel;
            if (query == null) {
                return;
            }
            NativeSession session = (NativeSession) query.getSession();
            if (session == null) {
                return;
            }

            try {
                killQuery(query, session);
            } catch (Throwable t) {
                caughtWhileCancelling = t;
            } finally {
                setQueryToCancel(null);
            }
        });
    }

    private void killQuery(Query localQueryToCancel, NativeSession session) throws IOException {
        synchronized (localQueryToCancel.getCancelTimeoutMutex()) {
            long origConnId = session.getThreadId();
            HostInfo hostInfo = session.getHostInfo();
            String database = hostInfo.getDatabase();

            CloudAuthProvider authProvider = authProvider(hostInfo);
            String user = authProvider != null ? authProvider.user() : hostInfo.getUser();
            String password = authProvider != null ? authProvider.accessToken() : hostInfo.getPassword();

            LOGGER.warn("kill query due to timeout, processId={}, query={}", origConnId, localQueryToCancel);

            NativeSession newSession = null;
            try {
                newSession = new NativeSession(hostInfo, session.getPropertySet());
                newSession.connect(hostInfo, user, password, database, 30000, new DefaultTransactionEventHandler());
                newSession.getProtocol().sendCommand(new NativeMessageBuilder(newSession.getServerSession().supportsQueryAttributes())
                    .buildComQuery(newSession.getSharedSendPacket(), "KILL QUERY " + origConnId), false, 0);
            } finally {
                close(newSession);
            }
            localQueryToCancel.setCancelStatus(CancelStatus.CANCELED_BY_TIMEOUT);
        }
    }

    @Nullable
    private CloudAuthProvider authProvider(HostInfo hostInfo) {
        if (hostInfo.getHostProperties().get(DatabaseImpl.PROPERTY_KEY_AUTH_PROVIDER) != null) {
            return GCloudAuthProvider.INSTANCE;
        }
        return null;
    }

    private void close(NativeSession session) {
        try {
            if (session != null) session.forceClose();
        } catch (Throwable e) {
            LOGGER.warn("failed to close session", e);
        }
    }

    @Override
    public Throwable getCaughtWhileCancelling() {
        return this.caughtWhileCancelling;
    }

    @Override
    public void setCaughtWhileCancelling(Throwable caughtWhileCancelling) {
        this.caughtWhileCancelling = caughtWhileCancelling;
    }

    @Override
    public Query getQueryToCancel() {
        return this.queryToCancel;
    }

    @Override
    public void setQueryToCancel(Query queryToCancel) {
        this.queryToCancel = queryToCancel;
    }

    private static final class DefaultTransactionEventHandler implements TransactionEventHandler {
        @Override
        public void transactionBegun() {
        }

        @Override
        public void transactionCompleted() {
        }
    }
}
