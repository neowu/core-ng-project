package core.framework.impl.web.request;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathParamsTest {
    private PathParams pathParams;

    @BeforeEach
    void createPathParams() {
        pathParams = new PathParams();
    }

    @Test
    void putEmptyPathParam() {
        assertThatThrownBy(() -> pathParams.put("id", ""))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("path param must not be empty");

        assertThatThrownBy(() -> pathParams.put("id", "invalidURIValue%"))
                .isInstanceOf(BadRequestException.class);
    }
}
