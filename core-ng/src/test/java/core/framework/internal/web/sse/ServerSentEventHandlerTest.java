package core.framework.internal.web.sse;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerSentEventHandlerTest {
    private ServerSentEventHandler handler;

    @BeforeEach
    void createServerSentEventHandler() {
        handler = new ServerSentEventHandler(null, null, null);
    }

    @Test
    void errorResponse() {
        byte[] error = handler.errorResponse(Strings.bytes("{\"error_code\": \"NOT_FOUND\"}"));
        assertThat(error).asString().isEqualTo("""
            retry: 86400000
            
            event: error
            data: {"error_code": "NOT_FOUND"}
            
            """);
    }
}
