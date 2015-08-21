package core.framework.impl.web.session;

import core.framework.api.scheduler.Job;

/**
 * @author neo
 */
public class LocalSessionStoreCleanupJob implements Job {
    private final LocalSessionStore sessionStore;

    public LocalSessionStoreCleanupJob(LocalSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void execute() throws Exception {
        sessionStore.cleanup();
    }
}
