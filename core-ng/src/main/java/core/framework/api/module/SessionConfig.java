package core.framework.api.module;

import core.framework.api.redis.Redis;
import core.framework.api.redis.RedisBuilder;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.FixedRateTrigger;
import core.framework.impl.web.session.LocalSessionStore;
import core.framework.impl.web.session.LocalSessionStoreCleanupJob;
import core.framework.impl.web.session.RedisSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class SessionConfig {
    private final Logger logger = LoggerFactory.getLogger(SessionConfig.class);
    private final ModuleContext context;

    public SessionConfig(ModuleContext context) {
        this.context = context;
    }

    public SessionConfig timeout(Duration timeout) {
        context.httpServer.siteManager.sessionManager.sessionTimeout(timeout);
        return this;
    }

    public SessionConfig local() {
        logger.info("create local session provider");
        LocalSessionStore sessionStore = new LocalSessionStore();
        context.scheduler().addTrigger(new FixedRateTrigger("local-session-cleanup", new LocalSessionStoreCleanupJob(sessionStore), Duration.ofMinutes(30)));
        context.httpServer.siteManager.sessionManager.sessionStore(sessionStore);
        return this;
    }

    public SessionConfig redis(String host) {
        if (context.test) {
            logger.info("use local session during test");
            return local();
        }

        logger.info("create redis session provider, host={}", host);
        Redis redis = new RedisBuilder().host(host).get();
        context.shutdownHook.add(redis::shutdown);
        context.httpServer.siteManager.sessionManager.sessionStore(new RedisSessionStore(redis));
        return this;
    }
}
