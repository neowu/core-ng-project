package core.framework.internal.http;

import okhttp3.Dns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
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
    private final ExtendedResolver resolver;

    public DNSManager(String... nameServers) {
        if (nameServers.length == 0) throw new Error("nameServers must not be empty");
        try {
            resolver = new ExtendedResolver(nameServers);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        Lookup lookup = createLookup(hostname);
        Record[] records = lookup.run();
        if (records == null || lookup.getResult() != Lookup.SUCCESSFUL) {
            logger.warn("failed to resolve host, host={}, fallback to system DNS", hostname);
            return List.of(InetAddress.getAllByName(hostname));
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
    }

    @SuppressWarnings("AvoidHidingCauseException")
    Lookup createLookup(String hostname) throws UnknownHostException {
        if (hostname == null) throw new UnknownHostException("hostname must not be null");
        try {
            var lookup = new Lookup(hostname);
            lookup.setResolver(resolver);
            return lookup;
        } catch (TextParseException e) {
            var exception = new UnknownHostException("failed to parse hostname, host=" + hostname);
            exception.initCause(e);
            throw exception;
        }
    }
}
