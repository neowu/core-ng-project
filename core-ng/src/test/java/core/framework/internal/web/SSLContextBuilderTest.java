package core.framework.internal.web;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SSLContextBuilderTest {
    @Test
    void build() {
        SSLContext context = new SSLContextBuilder().build();
        assertThat(context).isNotNull();
    }
}
