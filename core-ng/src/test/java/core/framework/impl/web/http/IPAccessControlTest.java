package core.framework.impl.web.http;

import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
        accessControl.allowCIDR("100.100.100.100/32");
    }

    @Test
    void validateClientIPWithMatchedIP() {
        accessControl.validateClientIP("100.100.100.100");
    }

    @Test
    void validateClientIPWithLocalIP() {
        accessControl.validateClientIP("127.0.0.1");
        accessControl.validateClientIP("192.168.0.1");
        accessControl.validateClientIP("10.0.0.1");
        accessControl.validateClientIP("::1");
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
    void validateClientIP() {
        assertThatThrownBy(() -> accessControl.validateClientIP("100.100.100.1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("denied");
    }
}
