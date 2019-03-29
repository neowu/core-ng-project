package core.log.web;

import core.framework.web.Request;
import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class EventControllerTest {
    private Request request;

    @BeforeEach
    void prepare() {
        request = mock(Request.class);
    }

    @Test
    void allowedOriginWithWildcard() {
        when(request.header("Origin")).thenReturn(Optional.of("https://localhost"));
        var controller = new EventController(Set.of("*"));
        String allowedOrigin = controller.allowedOrigin(request);
        assertThat(allowedOrigin).isEqualTo("*");
    }

    @Test
    void allowedOrigin() {
        when(request.header("Origin")).thenReturn(Optional.of("https://localhost"));
        var controller = new EventController(Set.of("https://local", "https://localhost"));
        assertThat(controller.allowedOrigin(request)).isEqualTo("https://localhost");
    }

    @Test
    void allowedOriginWithoutOriginHeader() {
        when(request.header("Origin")).thenReturn(Optional.empty());
        var controller = new EventController(Set.of("*"));
        assertThatThrownBy(() -> controller.allowedOrigin(request))
                .isInstanceOf(ForbiddenException.class);
    }
}
