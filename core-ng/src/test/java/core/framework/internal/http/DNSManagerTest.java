package core.framework.internal.http;

import org.junit.jupiter.api.Test;

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
}
