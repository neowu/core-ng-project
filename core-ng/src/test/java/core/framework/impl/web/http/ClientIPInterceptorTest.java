package core.framework.impl.web.http;

import core.framework.util.Sets;
import core.framework.web.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class ClientIPInterceptorTest {
    private ClientIPInterceptor interceptor;

    @BeforeEach
    void createAllowSourceIPInterceptor() {
        interceptor = new ClientIPInterceptor(Sets.newHashSet("100.100.100.100/32"));
    }

    @Test
    void validateClientIPWithMatchedIP() {
        interceptor.validateClientIP("100.100.100.100");
    }

    @Test
    void validateClientIPWithLocalIP() {
        interceptor.validateClientIP("127.0.0.1");
        interceptor.validateClientIP("192.168.0.1");
        interceptor.validateClientIP("10.0.0.1");
        interceptor.validateClientIP("::1");
    }

    @Test
    void validateClientIP() {
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> interceptor.validateClientIP("100.100.100.1"));
        assertThat(exception.getMessage()).contains("denied");
    }
}
