package core.framework.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.resource.PoolMetrics;
import core.framework.internal.web.session.LocalSessionStore;
import core.framework.internal.web.session.RedisSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class SessionConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(SessionConfig.class);
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        cookie("SessionId", null);
    }

    public void timeout(Duration timeout) {
        context.httpServer.siteManager.sessionManager.timeout(timeout);
    }

    public void cookie(String name, String domain) {
        context.httpServer.siteManager.sessionManager.cookie(name, domain);
        context.logManager.maskFields(name);
    }

    public void header(String name) {
        context.httpServer.siteManager.sessionManager.header(name);
        context.logManager.maskFields(name);
    }

    public void local() {
        logger.info("create local session store");
        LocalSessionStore sessionStore = new LocalSessionStore();
        context.backgroundTask().scheduleWithFixedDelay(sessionStore::cleanup, Duration.ofMinutes(30));
        context.httpServer.siteManager.sessionManager.store(sessionStore);
    }

    public void redis(String host) {
        logger.info("create redis session store, host={}", host);

        var redis = new RedisImpl("redis-session");
        redis.host(host);
        context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
        context.collector.metrics.add(new PoolMetrics(redis.pool));

        context.probe.hostURIs.add(host);
        context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> redis.close());
        context.httpServer.siteManager.sessionManager.store(new RedisSessionStore(redis));
    }
}
