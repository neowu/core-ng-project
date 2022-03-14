package core.framework.internal.kafka;

import core.framework.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class KafkaURI {
    public final List<String> bootstrapURIs;
    private final String uri;

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

    @Override
    public String toString() {  // make it easier to log
        return uri;
    }
}
