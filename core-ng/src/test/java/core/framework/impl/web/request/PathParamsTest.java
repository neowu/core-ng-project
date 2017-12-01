package core.framework.impl.web.request;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        BadRequestException exception = assertThrows(BadRequestException.class, () -> pathParams.put("id", ""));
        assertThat(exception.getMessage()).contains("name=id, value=");
    }
}
