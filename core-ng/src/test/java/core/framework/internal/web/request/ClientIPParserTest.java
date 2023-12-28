package core.framework.internal.web.request;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ClientIPParserTest {
    private ClientIPParser parser;

    @BeforeEach
    void createClientIPParser() {
        parser = new ClientIPParser();
    }

    @Test
    void withoutProxy() {
        assertThat(parser.parse("127.0.0.1", null)).isEqualTo("127.0.0.1");
        assertThat(parser.parse("127.0.0.1", "")).isEqualTo("127.0.0.1");
    }

    @Test
    void withProxy() {
        assertThat(parser.parse("127.0.0.1", "108.0.0.1")).isEqualTo("108.0.0.1");
        assertThat(parser.parse("127.0.0.1", " 108.0.0.1 ")).isEqualTo("108.0.0.1");
        assertThat(parser.parse("127.0.0.1", "108.0.0.1, 10.10.10.10")).isEqualTo("108.0.0.1");
    }

    @Test
    void exceedMaxForwardedIPs() {
        assertThat(parser.parse("10.0.0.1", "192.168.0.1,108.0.0.1,10.0.0.1")).isEqualTo("108.0.0.1");
        assertThat(parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.1, 108.0.0.1, 10.0.0.1")).isEqualTo("108.0.0.1");

        parser.maxForwardedIPs = 1;
        assertThat(parser.parse("10.0.0.1", "192.168.0.1,108.0.0.1,10.0.0.1")).isEqualTo("10.0.0.1");
        assertThat(parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.1, 108.0.0.1, 10.0.0.1")).isEqualTo("10.0.0.1");

        parser.maxForwardedIPs = 2;
        assertThat(parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.2, 192.168.0.3")).isEqualTo("192.168.0.2");
        assertThat(parser.parse("10.0.0.1", "192.168.0.3")).isEqualTo("192.168.0.3");

        parser.maxForwardedIPs = 3;
        assertThat(parser.parse("10.0.0.1", "192.168.0.1, 192.168.0.2, 192.168.0.3, 192.168.0.4")).isEqualTo("192.168.0.2");
        assertThat(parser.parse("10.0.0.1", "192.168.0.3, 192.168.0.4")).isEqualTo("192.168.0.3");
    }

    @Test
    void illegalXForwardedFor() {
        assertThat(parser.parse("10.0.0.1", ",108.0.0.1,10.0.0.1")).isEqualTo("108.0.0.1");
        assertThat(parser.parse("10.0.0.1", "text , 108.0.0.1, 10.0.0.1")).isEqualTo("108.0.0.1");

        assertThatThrownBy(() -> parser.parse("10.0.0.1", "text , 10.0.0.1"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("invalid client ip address");
    }

    @Test
    void extractIP() {
        assertThat(parser.extractIP("2001:db8:cafe::17")).isEqualTo("2001:db8:cafe::17");
        assertThat(parser.extractIP("192.0.2.43:47011")).isEqualTo("192.0.2.43");
        assertThat(parser.extractIP("192.0.2.43")).isEqualTo("192.0.2.43");

        assertInvalidNode("192.0.2.43:");
        assertInvalidNode("192.0:2.43");
        assertInvalidNode("192.0.:2.43");
        assertInvalidNode("192.0.2:43");
    }

    @Test
    void hasMoreThanMaxForwardedIPs() {
        parser.maxForwardedIPs = 1;
        assertThat(parser.hasMoreThanMaxForwardedIPs(null)).isFalse();
        assertThat(parser.hasMoreThanMaxForwardedIPs("10.0.0.1")).isFalse();
        assertThat(parser.hasMoreThanMaxForwardedIPs("10.0.0.1, 10.0.0.2")).isTrue();

        parser.maxForwardedIPs = 2;
        assertThat(parser.hasMoreThanMaxForwardedIPs(" ")).isFalse();
        assertThat(parser.hasMoreThanMaxForwardedIPs("10.0.0.1, 10.0.0.2")).isFalse();
        assertThat(parser.hasMoreThanMaxForwardedIPs("10.0.0.1, 10.0.0.2, 10.0.0.3")).isTrue();
    }

    private void assertInvalidNode(String node) {
        assertThatThrownBy(() -> parser.extractIP(node))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("invalid client ip address");
    }
}
