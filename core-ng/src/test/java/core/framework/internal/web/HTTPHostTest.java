package core.framework.internal.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPHostTest {
    @Test
    void convertToString() {
        assertThat(HTTPHost.parse("127.0.0.2:80").toString()).isEqualTo("127.0.0.2:80");
    }

    @Test
    void parse() {
        HTTPHost host = HTTPHost.parse("8080");
        assertThat(host.host()).isEqualTo("0.0.0.0");
        assertThat(host.port()).isEqualTo(8080);
    }
}
