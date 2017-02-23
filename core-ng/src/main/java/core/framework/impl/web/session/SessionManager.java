package core.framework.impl.web.session;

import core.framework.api.util.Exceptions;
import core.framework.api.web.CookieSpec;
import core.framework.api.web.Session;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class SessionManager {
    private final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private CookieSpec sessionId = new CookieSpec("SessionId");  // default domain is null, which is current host
    private Duration timeout = Duration.ofMinutes(20);
    private SessionStore store;

    public Session load(RequestImpl request) {
        if (store == null) return null;  // session store is not initialized
        if (!"https".equals(request.scheme())) return null;  // only load session under https

        SessionImpl session = new SessionImpl();
        request.cookie(sessionId).ifPresent(sessionId -> {
            logger.debug("load http session");
            Map<String, String> values = store.getAndRefresh(sessionId, timeout);
            if (values != null) {
                logger.debug("[session] values={}", values);
                session.id = sessionId;
                session.values.putAll(values);
            }
        });
        return session;
    }

    public void save(RequestImpl request, HttpServerExchange exchange) {
        SessionImpl session = (SessionImpl) request.session;
        if (session == null) return;

        if (session.invalidated && session.id != null) {
            logger.debug("invalidate http session");
            store.invalidate(session.id);
            CookieImpl cookie = sessionCookie(request.scheme());
            cookie.setMaxAge(0);
            cookie.setValue("");
            exchange.setResponseCookie(cookie);
        } else if (session.changed()) {
            logger.debug("save http session");
            if (session.id == null) {
                session.id = UUID.randomUUID().toString();
                CookieImpl cookie = sessionCookie(request.scheme());
                cookie.setMaxAge(-1);
                cookie.setValue(session.id);
                exchange.setResponseCookie(cookie);
            }
            store.save(session.id, session.values, session.changedFields, timeout);
        }
    }

    public void sessionStore(SessionStore store) {
        if (this.store != null) throw Exceptions.error("session store is already configured, previous={}", this.store);
        this.store = store;
    }

    public void timeout(Duration timeout) {
        if (timeout == null) throw Exceptions.error("timeout must not be null");
        this.timeout = timeout;
    }

    public void cookie(String name, String domain) {
        if (name == null) throw Exceptions.error("name must not be null");
        sessionId = new CookieSpec(name).domain(domain);
    }

    private CookieImpl sessionCookie(String scheme) {
        CookieImpl cookie = new CookieImpl(sessionId.name);
        cookie.setDomain(sessionId.domain);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
