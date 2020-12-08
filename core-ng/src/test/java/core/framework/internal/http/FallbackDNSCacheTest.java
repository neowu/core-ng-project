package core.framework.internal.http;

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
class FallbackDNSCacheTest {
    private FallbackDNSCache dns;

    @BeforeEach
    void createFallbackDNSCache() {
        dns = new FallbackDNSCache();
    }

    @Test
    void lookup() throws UnknownHostException {
        assertThat(dns.lookup("localhost")).isNotNull();
        assertThat(dns.cache).containsKey("localhost");
    }

    @Test
    void lookupWithInvalidDomain() {
        assertThatThrownBy(() -> dns.lookup("www.invalid"))
                .isInstanceOf(UnknownHostException.class);
    }

    @Test
    void lookupWithFallback() throws UnknownHostException {
        dns.cache.put("test.invalid", List.of(InetAddress.getLocalHost()));
        assertThat(dns.lookup("test.invalid")).isNotNull();
    }
}
