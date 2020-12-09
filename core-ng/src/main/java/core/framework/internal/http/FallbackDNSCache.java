package core.framework.internal.http;

import core.framework.util.Maps;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public class FallbackDNSCache implements Dns {
    // usually http client only calls limited external domains, so here use unlimited cache
    final Map<String, List<InetAddress>> cache = Maps.newConcurrentHashMap();

    private final Logger logger = LoggerFactory.getLogger(FallbackDNSCache.class);

    @Override
    public List<InetAddress> lookup(String domain) throws UnknownHostException {
        try {
            List<InetAddress> addresses = Arrays.asList(InetAddress.getAllByName(domain));
            cache.put(domain, addresses);
            return addresses;
        } catch (UnknownHostException e) {
            List<InetAddress> addresses = cache.get(domain);
            if (addresses != null) {
                logger.warn(errorCode("DNS_FAILURE"), "failed to resolve domain, fallback to previous cached addresses, domain={}", domain, e);
                return addresses;
            }
            throw e;
        }
    }
}
