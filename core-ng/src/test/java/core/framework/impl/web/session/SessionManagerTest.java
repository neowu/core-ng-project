package core.framework.impl.web.session;

import core.framework.impl.log.ActionLog;
import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class SessionManagerTest {
    private SessionManager sessionManager;
    private LocalSessionStore store;

    @BeforeEach
    void createSessionManager() {
        store = new LocalSessionStore();
        sessionManager = new SessionManager();
        sessionManager.store(store);
    }

    @Test
    void header() {
        sessionManager.header("SessionId");
        ActionLog actionLog = new ActionLog(null);

        String sessionId = UUID.randomUUID().toString();
        store.save(sessionId, Map.of("key", "value"), Set.of(), Duration.ofMinutes(30));

        Request request = mock(Request.class);
        when(request.scheme()).thenReturn("https");
        when(request.header("SessionId")).thenReturn(Optional.of(sessionId));

        Session session = sessionManager.load(request, actionLog);
        assertThat(session).isNotNull();
        assertThat(session.get("key")).isNotNull().hasValue("value");
        assertThat(actionLog.context("sessionHash")).isNotEmpty();
    }

    @Test
    void cookie() {
        sessionManager.cookie("SessionId", null);

        String sessionId = UUID.randomUUID().toString();
        store.save(sessionId, Map.of("key", "value"), Set.of(), Duration.ofMinutes(30));

        Request request = mock(Request.class);
        when(request.scheme()).thenReturn("https");
        when(request.cookie(eq(new CookieSpec("SessionId").path("/")))).thenReturn(Optional.of(sessionId));

        Session session = sessionManager.load(request, new ActionLog(null));
        assertThat(session).isNotNull();
        assertThat(session.get("key")).isNotNull().hasValue("value");
    }

    @Test
    void putSessionIdToHeader() {
        sessionManager.header("SessionId");
        Response response = mock(Response.class);

        String sessionId = UUID.randomUUID().toString();
        sessionManager.putSessionId(response, sessionId);

        verify(response).header("SessionId", sessionId);
    }

    @Test
    void putSessionIdToCookie() {
        sessionManager.cookie("SessionId", null);
        Response response = mock(Response.class);

        String sessionId = UUID.randomUUID().toString();
        sessionManager.putSessionId(response, sessionId);

        verify(response).cookie(eq(new CookieSpec("SessionId").path("/")), eq(sessionId));
    }

    @Test
    void saveWithInvalidatedSessionWithoutId() {
        Response response = mock(Response.class);
        var session = new SessionImpl();
        session.invalidate();
        sessionManager.save(session, response, new ActionLog(null));

        verifyZeroInteractions(response);
    }

    @Test
    void saveWithInvalidatedSession() {
        sessionManager.header("SessionId");
        Response response = mock(Response.class);

        var session = new SessionImpl();
        session.id = UUID.randomUUID().toString();
        session.invalidate();
        sessionManager.save(session, response, new ActionLog(null));

        verify(response).header("SessionId", "");
    }

    @Test
    void saveNewSession() {
        sessionManager.header("SessionId");
        ActionLog actionLog = new ActionLog(null);
        Response response = mock(Response.class);

        var session = new SessionImpl();
        session.set("key", "value");
        sessionManager.save(session, response, actionLog);

        assertThat(actionLog.context("sessionHash")).isNotEmpty();
        verify(response).header(eq("SessionId"), anyString());
    }
}
