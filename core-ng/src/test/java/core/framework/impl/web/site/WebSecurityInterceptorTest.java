package core.framework.impl.web.site;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebSecurityInterceptorTest {
    private WebSecurityInterceptor interceptor;

    @BeforeEach
    void createWebSecurityInterceptor() {
        interceptor = new WebSecurityInterceptor();
    }

    @Test
    void contentSecurityPolicy() {
        assertThat(interceptor.contentSecurityPolicy()).isEqualTo("default-src https:; object-src 'none'; frame-src 'none';");
        assertThat(interceptor.contentSecurityPolicy("https://cdn", "https://ga")).isEqualTo("default-src 'self' https://cdn https://ga; object-src 'none'; frame-src 'none';");
    }
}
