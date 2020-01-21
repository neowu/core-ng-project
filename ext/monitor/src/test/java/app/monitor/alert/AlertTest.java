package app.monitor.alert;

import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class AlertTest {
    @Test
    void severity() {
        Alert alert = new Alert();
        alert.severity("WARN");
        assertThat(alert.severity).isEqualTo(Severity.WARN);

        alert.severity("ERROR");
        assertThat(alert.severity).isEqualTo(Severity.ERROR);
    }
}
