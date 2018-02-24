package core.framework.impl.web;

import io.undertow.io.Sender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class HTTPServerHealthCheckHandlerTest {
    private HTTPServerHealthCheckHandler handler;
    private Sender sender;

    @BeforeEach
    void createHTTPServerHealthCheckHandler() {
        sender = Mockito.mock(Sender.class);
        handler = new HTTPServerHealthCheckHandler();
    }

    @Test
    void handle() {
        handler.handle(sender);

        verify(sender).send(eq(ByteBuffer.wrap(new byte[0])));
    }
}
