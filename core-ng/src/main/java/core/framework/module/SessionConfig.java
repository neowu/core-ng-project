package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.PoolMetrics;
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

    SessionConfig(ModuleContext context) {
        this.context = context;
        context.logManager.filter.maskedFields.add(context.httpServer.siteManager.sessionManager.sessionId.name);
    }

    public void timeout(Duration timeout) {
        context.httpServer.siteManager.sessionManager.timeout(timeout);
    }

    public void cookie(String name, String domain) {
        context.httpServer.siteManager.sessionManager.cookie(name, domain);
        context.logManager.filter.maskedFields.add(name);
    }

    public void local() {
        logger.info("create local session provider");
        LocalSessionStore sessionStore = new LocalSessionStore();
        context.backgroundTask().scheduleWithFixedDelay(sessionStore::cleanup, Duration.ofMinutes(30));
        context.httpServer.siteManager.sessionManager.sessionStore(sessionStore);
    }

    public void redis(String host) {
        if (context.isTest()) {
            local();
        } else {
            logger.info("create redis session provider, host={}", host);

            RedisImpl redis = new RedisImpl("redis-session");
            redis.host = host;
            context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
            context.stat.metrics.add(new PoolMetrics(redis.pool));

            context.shutdownHook.add(redis::close);
            context.httpServer.siteManager.sessionManager.sessionStore(new RedisSessionStore(redis));
        }
    }
}
