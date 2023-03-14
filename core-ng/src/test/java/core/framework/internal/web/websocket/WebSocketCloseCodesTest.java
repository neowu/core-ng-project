package core.framework.internal.web.websocket;

import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.TooManyRequestsException;
import core.framework.web.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebSocketCloseCodesTest {
    @Test
    void closeCode() {
        assertThat(WebSocketCloseCodes.closeCode(new Error()))
            .isEqualTo(WebSocketCloseCodes.INTERNAL_ERROR);

        assertThat(WebSocketCloseCodes.closeCode(new TooManyRequestsException("rate exceeds")))
            .isEqualTo(WebSocketCloseCodes.TRY_AGAIN_LATER);

        assertThat(WebSocketCloseCodes.closeCode(new BadRequestException("bad request")))
            .isEqualTo(WebSocketCloseCodes.POLICY_VIOLATION);

        assertThat(WebSocketCloseCodes.closeCode(new UnauthorizedException("login failed")))
            .isEqualTo(WebSocketCloseCodes.POLICY_VIOLATION);
    }
}
