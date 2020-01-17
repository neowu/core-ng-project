package core.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LogCollectorAppTest {
    private LogCollectorApp app;

    @BeforeEach
    void createLogCollectorApp() {
        app = new LogCollectorApp();
    }

    @Test
    void allowedOrigins() {
        Set<String> origins = app.allowedOrigins("origin1, \norigin2  ,\n\rorigin3");
        assertThat(origins).containsExactlyInAnyOrder("origin1", "origin2", "origin3");
    }

}
