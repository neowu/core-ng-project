package core.framework.internal.web.site;

import core.framework.web.Request;
import core.framework.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class StaticDirectoryControllerTest {
    @Mock
    Request request;
    private StaticDirectoryController controller;

    @BeforeEach
    void createStaticDirectoryController() {
        controller = new StaticDirectoryController(Path.of("/opt/app/web/static"));
    }

    @Test
    void cache() {
        controller.cache(Duration.ofMinutes(10));

        assertEquals("public, max-age=600", controller.cacheHeader);
    }

    @Test
    void illegalAccess() {
        when(request.pathParam("path")).thenReturn("//////../../../../etc/passwd");

        assertThatThrownBy(() -> controller.execute(request))
            .isInstanceOf(NotFoundException.class);
    }
}
