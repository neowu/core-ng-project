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
class AllowSourceIPInterceptorTest {
    private AllowSourceIPInterceptor interceptor;

    @BeforeEach
    void createAllowSourceIPInterceptor() {
        interceptor = new AllowSourceIPInterceptor(Sets.newHashSet("100.100.100.100"));
    }

    @Test
    void validateSourceIPWithAllowedIP() {
        interceptor.validateSourceIP("100.100.100.100");
    }

    @Test
    void validateSourceIPWithLocalIP() {
        interceptor.validateSourceIP("127.0.0.1");
        interceptor.validateSourceIP("192.168.0.1");
        interceptor.validateSourceIP("10.0.0.1");
        interceptor.validateSourceIP("::1");
    }

    @Test
    void validateSourceIP() {
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> interceptor.validateSourceIP("100.100.100.1"));
        assertThat(exception.getMessage()).contains("denied");
    }
}
