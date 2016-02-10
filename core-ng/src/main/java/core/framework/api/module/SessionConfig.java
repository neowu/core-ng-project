package core.framework.api.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.web.session.LocalSessionStore;
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

    public void timeout(Duration timeout) {
        context.httpServer.siteManager.sessionManager.sessionTimeout(timeout);
    }

    public void local() {
        logger.info("create local session provider");
        LocalSessionStore sessionStore = new LocalSessionStore();
        if (!context.isTest()) {
            context.backgroundTask().scheduleWithFixedDelay(sessionStore::cleanup, Duration.ofMinutes(30));
        }
        context.httpServer.siteManager.sessionManager.sessionStore(sessionStore);
    }

    public void redis(String host) {
        if (context.isTest()) {
            logger.info("use local session during test");
            local();
        } else {
            logger.info("create redis session provider, host={}", host);

            RedisImpl redis = new RedisImpl();
            redis.host(host);
            redis.pool.name("redis-session");
            context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));

            context.shutdownHook.add(redis::close);
            context.httpServer.siteManager.sessionManager.sessionStore(new RedisSessionStore(redis));
        }
    }
}
