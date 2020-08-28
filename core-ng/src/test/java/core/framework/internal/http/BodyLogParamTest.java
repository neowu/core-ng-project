package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BodyLogParamTest {
    @Test
    void bodyParam() {
        assertThat(BodyLogParam.of(Strings.bytes("{}"), ContentType.APPLICATION_JSON))
                .isInstanceOf(BytesLogParam.class);

        assertThat(BodyLogParam.of(Strings.bytes("<xml/>"), ContentType.TEXT_XML))
                .isInstanceOf(BytesLogParam.class);

        assertThat(BodyLogParam.of(Strings.bytes("value"), null))
                .isEqualTo("byte[5]");
        assertThat(BodyLogParam.of(new byte[10], ContentType.IMAGE_PNG))
                .isEqualTo("byte[10]");
    }
}
