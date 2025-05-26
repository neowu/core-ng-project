package core.framework.internal.web.http;

import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static core.framework.internal.web.http.IPRanges.address;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class IPAccessControlTest {
    private IPAccessControl accessControl;

    @BeforeEach
    void createIPAccessControl() {
        accessControl = new IPAccessControl();
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
        var deny = new IPv4Ranges(List.of("100.100.100.100/24"));
        var allow = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(address("100.100.99.1"), allow, deny)).isTrue();
        assertThat(accessControl.allow(address("100.100.100.101"), allow, deny)).isTrue();
        assertThat(accessControl.allow(address("100.100.100.1"), allow, deny)).isFalse();
    }

    @Test
    void allowWithOnlyAllowDefined() {
        var allow = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(address("100.100.99.1"), allow, null)).isFalse();
        assertThat(accessControl.allow(address("100.100.100.101"), allow, null)).isTrue();
        assertThat(accessControl.allow(address("100.100.100.1"), allow, null)).isFalse();
    }

    @Test
    void allowWithOnlyDenyDefined() {
        var deny = new IPv4Ranges(List.of("100.100.100.101/32"));

        assertThat(accessControl.allow(address("100.100.99.1"), null, deny)).isTrue();
        assertThat(accessControl.allow(address("100.100.100.101"), null, deny)).isFalse();
        assertThat(accessControl.allow(address("100.100.100.1"), null, deny)).isTrue();
    }

    @Test
    void denyByDefault() {
        assertThat(accessControl.allow(address("100.100.100.100"), null, null)).isFalse();

        assertThat(accessControl.allow(address("2001:0db8:85a3:0000:0000:8a2e:0370:0000"), null, null)).isFalse();
    }
}
