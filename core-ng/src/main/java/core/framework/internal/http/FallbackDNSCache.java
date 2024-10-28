package core.framework.internal.http;

import core.framework.util.Maps;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public class FallbackDNSCache implements Dns {
    private static final int TTL = 3_600_000;   // in millis, 1 hour

    // usually http client only calls limited external domains, so here use unlimited cache
    final Map<String, Entry> cache = Maps.newConcurrentHashMap();

    private final Logger logger = LoggerFactory.getLogger(FallbackDNSCache.class);
    private final Clock clock;

    public FallbackDNSCache(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<InetAddress> lookup(String domain) throws UnknownHostException {
        try {
            List<InetAddress> addresses = Arrays.asList(InetAddress.getAllByName(domain));
            cache.put(domain, new Entry(addresses, clock.millis() + TTL));
            return addresses;
        } catch (UnknownHostException e) {
            Entry entry = cache.get(domain);
            if (entry != null) {
                if (entry.ttl < clock.millis()) {
                    cache.remove(domain);
                } else {
                    logger.warn(errorCode("DNS_FAILURE"), "failed to resolve domain, fallback to previous cached addresses, domain={}", domain, e);
                    return entry.addresses;
                }
            }
            throw e;
        }
    }

    static class Entry {
        List<InetAddress> addresses;
        long ttl;

        Entry(List<InetAddress> addresses, long ttl) {
            this.addresses = addresses;
            this.ttl = ttl;
        }
    }
}
