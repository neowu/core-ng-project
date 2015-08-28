package core.framework.impl.web.session;

import core.framework.api.scheduler.Job;

/**
 * @author neo
 */
public class CleanupLocalSessionStoreJob implements Job {
    private final LocalSessionStore sessionStore;

    public CleanupLocalSessionStoreJob(LocalSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public void execute() throws Exception {
        sessionStore.cleanup();
    }
}
