package core.framework.internal.web.response;

import io.undertow.io.Sender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ByteArrayBodyTest {
    @Mock
    Sender sender;

    @Test
    void send() {
        var body = new ByteArrayBody(new byte[10]);
        body.send(sender, null);

        verify(sender).send(any(ByteBuffer.class));
    }
}
