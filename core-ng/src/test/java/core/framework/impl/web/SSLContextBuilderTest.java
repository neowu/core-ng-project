package core.framework.impl.web;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class SSLContextBuilderTest {
    @Test
    void build() {
        SSLContext context = new SSLContextBuilder().build();
        assertNotNull(context);
    }
}
