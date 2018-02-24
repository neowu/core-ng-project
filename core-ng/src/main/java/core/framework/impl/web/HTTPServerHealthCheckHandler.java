package core.framework.impl.web;

import io.undertow.io.Sender;

import java.nio.ByteBuffer;

/**
 * @author neo
 */
public class HTTPServerHealthCheckHandler {
    public static final String PATH = "/health-check";
    private final byte[] body = new byte[0];

    public void handle(Sender sender) {
        sender.send(ByteBuffer.wrap(body));
    }
}
