package core.framework.impl.web.session;

import core.framework.api.util.Exceptions;
import core.framework.api.web.Session;
import core.framework.impl.web.RequestImpl;
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
    private SessionStore sessionStore;
    private Duration sessionTimeout = Duration.ofMinutes(20);

    public Session load(RequestImpl request) {
        if (sessionStore == null) return null;

        logger.debug("load http session");
        SessionImpl session = new SessionImpl();

        request.cookie("SessionId").ifPresent(sessionId -> {
            Map<String, String> sessionData = sessionStore.getAndRefresh(sessionId, sessionTimeout);
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

        logger.debug("save http session");
        if (session.invalidated && session.id != null) {
            sessionStore.clear(session.id);
            CookieImpl cookie = sessionCookie(request.scheme());
            cookie.setMaxAge(0);
            cookie.setValue("");
            exchange.setResponseCookie(cookie);
        } else if (session.changed) {
            if (session.id == null) {
                session.id = UUID.randomUUID().toString();
                CookieImpl cookie = sessionCookie(request.scheme());
                cookie.setMaxAge(-1);
                cookie.setValue(session.id);
                exchange.setResponseCookie(cookie);
            }
            sessionStore.save(session.id, session.data, sessionTimeout);
        }
    }

    public void sessionProvider(SessionStore sessionStore) {
        if (this.sessionStore != null)
            throw Exceptions.error("session store is already configured, previous={}", this.sessionStore);
        this.sessionStore = sessionStore;
    }

    public void sessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    private CookieImpl sessionCookie(String scheme) {
        CookieImpl cookie = new CookieImpl("SessionId");
        cookie.setPath("/");
        cookie.setSecure("https".equals(scheme));
        cookie.setHttpOnly(true);
        return cookie;
    }
}
