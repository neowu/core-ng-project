package core.framework.impl.web.response;

import io.undertow.io.Sender;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class ByteArrayBodyTest {
    @Test
    void send() {
        var sender = mock(Sender.class);
        var body = new ByteArrayBody(new byte[10]);
        body.send(sender, null);

        verify(sender).send(any(ByteBuffer.class));
    }
}
