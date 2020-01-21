package app.monitor.action;

import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionAlertTest {
    @Test
    void severity() {
        ActionAlert alert = new ActionAlert();
        alert.severity("WARN");
        assertThat(alert.severity).isEqualTo(Severity.WARN);

        alert.severity("ERROR");
        assertThat(alert.severity).isEqualTo(Severity.ERROR);
    }
}
