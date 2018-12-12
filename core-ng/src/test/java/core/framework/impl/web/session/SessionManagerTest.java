package core.framework.impl.web.session;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        String sessionId = UUID.randomUUID().toString();
        store.save(sessionId, Map.of("key", "value"), Set.of(), Duration.ofMinutes(30));

        Request request = mock(Request.class);
        when(request.scheme()).thenReturn("https");
        when(request.header("SessionId")).thenReturn(Optional.of(sessionId));

        Session session = sessionManager.load(request);
        assertThat(session).isNotNull();
        assertThat(session.get("key")).isNotNull().hasValue("value");
    }

    @Test
    void cookie() {
        sessionManager.cookie("SessionId", null);

        String sessionId = UUID.randomUUID().toString();
        store.save(sessionId, Map.of("key", "value"), Set.of(), Duration.ofMinutes(30));

        Request request = mock(Request.class);
        when(request.scheme()).thenReturn("https");
        when(request.cookie(eq(new CookieSpec("SessionId").path("/")))).thenReturn(Optional.of(sessionId));

        Session session = sessionManager.load(request);
        assertThat(session).isNotNull();
        assertThat(session.get("key")).isNotNull().hasValue("value");
    }

    @Test
    void responseSessionIdToHeader() {
        sessionManager.header("SessionId");
        Response response = mock(Response.class);

        String sessionId = UUID.randomUUID().toString();
        sessionManager.responseSessionId(response, sessionId);

        verify(response).header("SessionId", sessionId);
    }

    @Test
    void responseSessionIdToCookie() {
        sessionManager.cookie("SessionId", null);
        Response response = mock(Response.class);

        String sessionId = UUID.randomUUID().toString();
        sessionManager.responseSessionId(response, sessionId);

        verify(response).cookie(eq(new CookieSpec("SessionId").path("/")), eq(sessionId));
    }
}
