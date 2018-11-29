package core.framework.internal.http;

import core.framework.http.HTTPClientException;
import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class DNSManager implements Dns {
    private final Logger logger = LoggerFactory.getLogger(DNSManager.class);
    private final SimpleResolver resolver;

    public DNSManager(String dns) {
        try {
            resolver = new SimpleResolver(dns);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            var lookup = new Lookup(hostname);
            lookup.setResolver(resolver);
            Record[] records = lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL || records == null) {
                logger.warn("failed to resolve host, host={}, fallback to system DNS", hostname);
                return Dns.SYSTEM.lookup(hostname);
            }
            List<InetAddress> addresses = new ArrayList<>(records.length);
            for (Record record : records) {
                int type = record.getType();
                switch (type) {
                    case Type.A:
                        addresses.add(((ARecord) record).getAddress());
                        break;
                    case Type.AAAA:
                        addresses.add(((AAAARecord) record).getAddress());
                        break;
                    default:
                        throw new Error("not supported record type, type=" + type);
                }
            }
            return addresses;
        } catch (TextParseException e) {
            throw new HTTPClientException("invalid host, host=" + hostname, "INVALID_URL", e);
        }
    }
}
