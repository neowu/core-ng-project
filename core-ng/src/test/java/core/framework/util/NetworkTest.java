package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class NetworkTest {
    @Test
    void localHostAddress() {
        assertThat(Network.LOCAL_HOST_ADDRESS).isNotNull();
    }

    @Test
    void localHostName() {
        assertThat(Network.LOCAL_HOST_NAME).isNotNull();
    }
}
