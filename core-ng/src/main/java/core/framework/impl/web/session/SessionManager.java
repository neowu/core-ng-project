package core.framework.impl.web.session;

import core.framework.impl.web.request.RequestImpl;
import core.framework.util.Exceptions;
import core.framework.web.CookieSpec;
import core.framework.web.Response;
import core.framework.web.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public final class SessionManager {
    private final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private CookieSpec sessionId;
    private Duration timeout = Duration.ofMinutes(20);
    private SessionStore store;

    public SessionManager() {
        cookie("SessionId", null);  // default domain is null, which is current host
    }

    public Session load(RequestImpl request) {
        if (store == null) return null;  // session store is not initialized
        if (!"https".equals(request.scheme())) return null;  // only load session under https

        SessionImpl session = new SessionImpl();
        request.cookie(sessionId).ifPresent(sessionId -> {
            logger.debug("load session");
            Map<String, String> values = store.getAndRefresh(sessionId, timeout);
            if (values != null) {
                logger.debug("[session] values={}", values);
                session.id = sessionId;
                session.values.putAll(values);
            }
        });
        return session;
    }

    public void save(RequestImpl request, Response response) {
        SessionImpl session = (SessionImpl) request.session;
        if (session == null) return;

        if (session.invalidated && session.id != null) {
            logger.debug("invalidate session");
            store.invalidate(session.id);
            response.cookie(sessionId, null);
        } else if (session.changed()) {
            logger.debug("save session");
            if (session.id == null) {
                session.id = UUID.randomUUID().toString();
                response.cookie(sessionId, session.id);
            }
            store.save(session.id, session.values, session.changedFields, timeout);
        }
    }

    public void sessionStore(SessionStore store) {
        if (this.store != null) throw Exceptions.error("session store is already configured, previous={}", this.store);
        this.store = store;
    }

    public void timeout(Duration timeout) {
        if (timeout == null) throw new Error("timeout must not be null");
        this.timeout = timeout;
    }

    public void cookie(String name, String domain) {
        if (name == null) throw new Error("name must not be null");
        sessionId = new CookieSpec(name).domain(domain).path("/").sessionScope().httpOnly().secure();
    }
}
