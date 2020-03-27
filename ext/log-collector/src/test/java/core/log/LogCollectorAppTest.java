package core.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        List<String> origins = app.allowedOrigins("origin1, \norigin2  ,\n\rorigin3");
        assertThat(origins).containsExactly("origin1", "origin2", "origin3");
    }

    @Test
    void collectCookies() {
        assertThat(app.collectCookies(null)).isNull();

        List<String> cookies = app.collectCookies("visitor_id");
        assertThat(cookies).containsExactly("visitor_id");
    }
}
