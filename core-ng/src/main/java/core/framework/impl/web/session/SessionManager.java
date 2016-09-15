package core.framework.impl.web.session;

import core.framework.api.util.Exceptions;
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
    private String cookieName = "SessionId";
    private String cookieDomain;     // default to be null, which will be current host
    private Duration timeout = Duration.ofMinutes(20);
    private SessionStore store;

    public Session load(RequestImpl request) {
        if (store == null) return null;  // session store is not initialized

        logger.debug("load http session");
        SessionImpl session = new SessionImpl();

        request.cookie(cookieName).ifPresent(sessionId -> {
            Map<String, String> sessionData = store.getAndRefresh(sessionId, timeout);
            if (sessionData != null) {
                session.id = sessionId;
                session.data.putAll(sessionData);
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
        } else if (session.changed) {
            logger.debug("save http session");
            if (session.id == null) {
                session.id = UUID.randomUUID().toString();
                CookieImpl cookie = sessionCookie(request.scheme());
                cookie.setMaxAge(-1);
                cookie.setValue(session.id);
                exchange.setResponseCookie(cookie);
            }
            store.save(session.id, session.data, timeout);
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
        cookieName = name;
        cookieDomain = domain;
    }

    private CookieImpl sessionCookie(String scheme) {
        CookieImpl cookie = new CookieImpl(cookieName);
        cookie.setDomain(cookieDomain);
        cookie.setPath("/");
        cookie.setSecure("https".equals(scheme));
        cookie.setHttpOnly(true);
        return cookie;
    }
}
