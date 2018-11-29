package core.framework.internal.http;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DNSManagerTest {
    @Test
    void emptyNameServers() {
        assertThatThrownBy(DNSManager::new)
                .isInstanceOf(Error.class)
                .hasMessageContaining("nameServers must not be empty");
    }

    @Test
    void emptyHostname() {
        var manager = new DNSManager("8.8.8.8");

        assertThatThrownBy(() -> manager.createLookup(null))
                .isInstanceOf(UnknownHostException.class)
                .hasMessageContaining("hostname must not be null");
    }

    @Test
    void invalidHostname() {
        var manager = new DNSManager("8.8.8.8");

        assertThatThrownBy(() -> manager.createLookup("..."))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(TextParseException.class)
                .hasMessageContaining("failed to parse hostname");
    }
}
