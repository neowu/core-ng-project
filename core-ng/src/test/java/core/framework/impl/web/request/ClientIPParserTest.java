package core.framework.impl.web.request;

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
    void isValidIP() {
        assertThat(parser.isValidIP("10.0.0.1")).isTrue();
        assertThat(parser.isValidIP("127.0.0.1")).isTrue();
        assertThat(parser.isValidIP("::0")).isTrue();

        assertThat(parser.isValidIP("g.0.0.1")).isFalse();
        assertThat(parser.isValidIP("0.0.1")).isFalse();
        assertThat(parser.isValidIP("::text")).isFalse();
    }
}
