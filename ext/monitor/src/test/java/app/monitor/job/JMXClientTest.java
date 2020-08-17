package app.monitor.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.remote.JMXConnector;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class JMXClientTest {
    @Mock
    JMXConnector connector;
    private JMXClient jmxClient;

    @BeforeEach
    void createJMXClient() {
        jmxClient = new JMXClient("localhost");
    }

    @Test
    void checkWithTerminatedConnector() throws IOException {
        when(connector.getConnectionId()).thenThrow(new IOException("Not connected"));
        assertThat(jmxClient.check(connector)).isFalse();
        verify(connector).close();
    }

    @Test
    void check() throws IOException {
        when(connector.getConnectionId()).thenReturn("connectionId");
        assertThat(jmxClient.check(connector)).isTrue();
    }
}
