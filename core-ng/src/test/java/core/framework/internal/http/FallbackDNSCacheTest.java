package core.framework.internal.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class FallbackDNSCacheTest {
    private FallbackDNSCache dns;
    private Clock clock;

    @BeforeEach
    void createFallbackDNSCache() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        dns = new FallbackDNSCache(clock);
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
        dns.cache.put("test.invalid", new FallbackDNSCache.Entry(List.of(InetAddress.getLocalHost()), clock.millis() + 1_000));
        assertThat(dns.lookup("test.invalid")).isNotNull();

        dns.cache.put("test.invalid", new FallbackDNSCache.Entry(List.of(InetAddress.getLocalHost()), clock.millis() - 1_000));
        assertThatThrownBy(() -> dns.lookup("test.invalid"))
            .isInstanceOf(UnknownHostException.class);
        assertThat(dns.cache).doesNotContainKey("test.invalid");
    }
}
