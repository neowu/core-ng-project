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
        var alert = new Alert();
        alert.severity("WARN");
        assertThat(alert.severity).isEqualTo(Severity.WARN);

        alert.severity("ERROR");
        assertThat(alert.severity).isEqualTo(Severity.ERROR);
    }


    @Test
    void docURL() {
        var alert = new Alert();
        alert.id = "test-id";
        alert.kibanaIndex = "test";
        alert.kibanaURL = "http://kibana:5601";
        assertThat(alert.docURL()).isEqualTo("http://kibana:5601/app/kibana#/doc/test-pattern/test-*?id=test-id&_g=()");
    }

}
