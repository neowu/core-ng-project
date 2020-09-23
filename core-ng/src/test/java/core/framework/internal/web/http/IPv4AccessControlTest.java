package core.framework.internal.web.http;

import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class IPv4AccessControlTest {
    private IPv4AccessControl accessControl;

    @BeforeEach
    void createIPv4AccessControl() {
        accessControl = new IPv4AccessControl();
    }

    @Test
    void validateWithAllowedIP() {
        accessControl.allow = new IPv4Ranges(List.of("100.100.100.100/32"));
        accessControl.validate("100.100.100.100");
    }

    @Test
    void validateWithLocalIP() {
        accessControl.validate("127.0.0.1");
        accessControl.validate("192.168.0.1");
        accessControl.validate("10.0.0.1");
        accessControl.validate("::1");
    }

    @Test
    void validateWithNotAllowedIP() {
        accessControl.allow = new IPv4Ranges(List.of("100.100.100.100/32"));
        assertThatThrownBy(() -> accessControl.validate("100.100.100.1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void isLocal() throws UnknownHostException {
        assertThat(accessControl.isLocal(InetAddress.getByName("127.0.0.1"))).isTrue();
        assertThat(accessControl.isLocal(InetAddress.getByName("192.168.0.1"))).isTrue();
        assertThat(accessControl.isLocal(InetAddress.getByName("10.0.0.1"))).isTrue();
        assertThat(accessControl.isLocal(InetAddress.getByName("::1"))).isTrue();

        assertThat(accessControl.isLocal(InetAddress.getByName("24.0.0.1"))).isFalse();
    }

    @Test
    void allowWithBothAllowDenyDefined() {
        accessControl.deny = new IPv4Ranges(List.of("100.100.100.100/24"));
        accessControl.allow = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(IPv4Ranges.address("100.100.99.1"))).isTrue();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.101"))).isTrue();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.1"))).isFalse();
    }

    @Test
    void allowWithOnlyAllowDefined() {
        accessControl.allow = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(IPv4Ranges.address("100.100.99.1"))).isFalse();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.101"))).isTrue();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.1"))).isFalse();
    }

    @Test
    void allowWithOnlyDenyDefined() {
        accessControl.deny = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(IPv4Ranges.address("100.100.99.1"))).isTrue();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.101"))).isFalse();
        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.1"))).isTrue();
    }

    @Test
    void denyByDefault() {
        accessControl.allow = null;
        accessControl.deny = null;

        assertThat(accessControl.allow(IPv4Ranges.address("100.100.100.100"))).isFalse();
    }

    @Test
    void allowWithIPv6() {
        assertThat(accessControl.allow(IPv4Ranges.address("2001:0db8:85a3:0000:0000:8a2e:0370:0000"))).isTrue();
    }
}
