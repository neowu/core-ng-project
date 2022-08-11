package app.monitor.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIWarningsTest {
    private APIWarnings warnings;

    @BeforeEach
    void createAPIWarnings() {
        warnings = new APIWarnings();
    }

    @Test
    void errorMessage() {
        warnings.add("error1");
        warnings.add("error2");
        warnings.add(true, "warning1");

        String message = warnings.errorMessage();
        assertThat(message)
            .isEqualToIgnoringNewLines("""
                *incompatible changes*
                * error1
                * error2
                *compatible changes*
                * warning1""");
    }
}
