package core.framework.internal.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class DefaultLoggerServiceProviderTest {
    @Test
    void initialize() {
        var provider = new DefaultLoggerServiceProvider();
        provider.initialize();

        assertThat(provider.getLoggerFactory()).isNotNull();
        assertThat(provider.getMarkerFactory()).isNotNull();
        assertThat(provider.getMDCAdapter()).isNotNull();
        assertThat(provider.getRequestedApiVersion()).isEqualTo("2.0.99");
    }
}
