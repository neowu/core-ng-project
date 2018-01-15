package core.framework.impl.web.http;

import core.framework.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void allow() {
        assertTrue(interceptor.allow("100.100.100.100"));
    }

    @Test
    void allowLocal() {
        assertTrue(interceptor.allow("127.0.0.1"));
        assertTrue(interceptor.allow("192.168.0.1"));
        assertTrue(interceptor.allow("10.0.0.1"));
        assertTrue(interceptor.allow("::1"));
    }
}
