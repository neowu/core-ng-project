package core.framework.internal.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class HTTPIOHandlerTest {
    @Mock
    HttpServerExchange exchange;
    private HTTPIOHandler handler;

    @BeforeEach
    void createHTTPIOHandler() {
        handler = new HTTPIOHandler(null, null, 1000);
    }

    @Test
    void hasBody() {
        assertThat(handler.hasBody(0, Methods.GET)).isFalse();
        assertThat(handler.hasBody(0, Methods.POST)).isFalse();
        assertThat(handler.hasBody(-1, Methods.POST)).isTrue();     // without content length header, Transfer-Encoding: chunked
        assertThat(handler.hasBody(100, Methods.PUT)).isTrue();
        assertThat(handler.hasBody(100, Methods.PATCH)).isTrue();
    }

    @Test
    void checkContentLength() {
        assertThat(handler.checkContentLength(1000, exchange)).isTrue();

        when(exchange.getRequestURL()).thenReturn("http://localhost/");
        assertThat(handler.checkContentLength(1001, exchange)).isFalse();
        verify(exchange).setStatusCode(413);
        verify(exchange).endExchange();
    }
}
