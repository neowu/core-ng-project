package core.framework.internal.web.session;

import core.framework.internal.log.ActionLog;
import core.framework.internal.web.request.RequestImpl;
import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.Session;
import core.framework.web.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public class SessionManager implements SessionContext {
    private final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private CookieSpec cookieSpec;
    private String header;
    private Duration timeout = Duration.ofMinutes(20);
    private SessionStore store;

    public Session load(Request request, ActionLog actionLog) {
        if (store == null) return null;  // session store is not initialized
        if (!"https".equals(request.scheme())) return null;  // only load session under https

        String domain = domain(request);
        var session = new SessionImpl(domain);
        sessionId(request).ifPresent(sessionId -> {
            Map<String, String> values = store.getAndRefresh(sessionId, session.domain, timeout);
            if (values != null) {
                session.id(sessionId);
                actionLog.context.put("session_hash", List.of(session.hash));
                session.values.putAll(values);
            }
            logger.debug("load session, domain={}, sessionHash={}", domain, session.hash);  // session.hash can be null if session is not found
        });
        return session;
    }

    public void save(RequestImpl request, Response response, ActionLog actionLog) {
        // request.session can be null or ReadOnlySession for websocket, only regular http request may generate session
        if (request.session instanceof SessionImpl session && !session.saved) {
            save(session, response, actionLog);
        }
    }

    private void save(SessionImpl session, Response response, ActionLog actionLog) {
        session.saved = true;   // it will try to save session on both normal and exception flows, here is to only attempt once in case of store throws exception
        if (session.invalidated) {
            if (session.id != null) {
                logger.debug("invalidate session, domain={}, sessionHash={}", session.domain, session.hash);
                store.invalidate(session.id, session.domain);
                putSessionId(response, null);
            }
        } else if (session.changed()) {
            if (session.id == null) {
                session.id(UUID.randomUUID().toString());
                actionLog.context.put("session_hash", List.of(session.hash));
                putSessionId(response, session.id);
            }
            logger.debug("save session, domain={}, sessionHash={}", session.domain, session.hash);
            store.save(session.id, session.domain, session.values, session.changedFields, timeout);
        }
    }

    private Optional<String> sessionId(Request request) {
        if (header != null) return request.header(header);
        return request.cookie(cookieSpec);
    }

    String domain(Request request) {
        // if header is specified, always use current host, otherwise use cookieSpec.domain if specified
        // for header/api scenario, there is no way to share sessionId across multiple apps, so always use request.host
        // for cookie/web scenario, only case to share session cookie is to have multiple sub-domain webapps, thus use cookieSpec.domain (usually parent domain)
        // share sessionId requires multiple webapps decode session key/values consistently, which adds extra complexity for dev/deployment
        if (header == null && cookieSpec.domain != null)
            return cookieSpec.domain;
        return request.hostname();
    }

    void putSessionId(Response response, String sessionId) {
        if (header != null) {
            response.header(header, sessionId == null ? "" : sessionId);
        } else {
            response.cookie(cookieSpec, sessionId);
        }
    }

    public void store(SessionStore store) {
        if (this.store != null) throw new Error("session store is already configured, previous=" + this.store);
        this.store = store;
    }

    public void timeout(Duration timeout) {
        if (timeout == null) throw new Error("timeout must not be null");
        this.timeout = timeout;
    }

    public void cookie(String name, String domain) {
        if (name == null) throw new Error("name must not be null");
        // path must be /, refer to https://datatracker.ietf.org/doc/html/rfc6265#section-4.1.2.4
        cookieSpec = new CookieSpec(name).domain(domain).path("/").sessionScope().httpOnly().secure().sameSite();
    }

    public void header(String name) {
        if (name == null) throw new Error("name must not be null");
        header = name;
    }

    @Override
    public void invalidate(String key, String value) {
        if (store == null) throw new Error("site().session() must be configured");
        if (key == null || value == null) throw new Error("key/value must not be null");   // to prevent from invalidating all sessions miss this key
        store.invalidateByKey(key, value);
    }
}
