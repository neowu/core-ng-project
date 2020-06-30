package core.framework.internal.kafka;

import core.framework.util.Strings;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class KafkaURI {
    public final List<String> bootstrapURIs;
    private final String uri;
    private boolean resolved;

    public KafkaURI(String uri) {
        this.uri = uri;
        bootstrapURIs = parse(uri);
    }

    // refer to org.apache.kafka.clients.ClientUtils.parseAndValidateAddresses,
    // append default port if not presents
    private List<String> parse(String uri) {
        String[] values = Strings.split(uri, ',');
        List<String> uris = new ArrayList<>(values.length);
        for (String value : values) {
            String result = value.strip();
            if (result.indexOf(':') == -1) {
                uris.add(result + ":9092");
            } else {
                uris.add(result);
            }
        }
        return uris;
    }

    public boolean resolveURI() {
        if (resolved) return true;

        for (String uri : bootstrapURIs) {
            int index = uri.indexOf(':');
            if (index == -1) throw new Error("invalid kafka uri, uri=" + uri);
            String host = uri.substring(0, index);
            var address = new InetSocketAddress(host, 9092);
            if (!address.isUnresolved()) {
                resolved = true;
                return true;    // break if any uri is resolvable
            }
        }

        return false;
    }

    @Override
    public String toString() {  // make it easier to log kafkaURI
        return uri;
    }
}
