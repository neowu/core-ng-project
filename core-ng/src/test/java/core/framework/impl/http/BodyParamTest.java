package core.framework.impl.http;

import core.framework.http.ContentType;
import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.log.filter.JSONParam;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BodyParamTest {
    @Test
    void bodyParam() {
        assertThat(BodyParam.param(Strings.bytes("{}"), ContentType.APPLICATION_JSON))
                .isInstanceOf(JSONParam.class);

        assertThat(BodyParam.param(Strings.bytes("value"), null))
                .isEqualTo("byte[5]");

        assertThat(BodyParam.param(Strings.bytes("<xml/>"), ContentType.TEXT_XML))
                .isInstanceOf(BytesParam.class);

        assertThat(BodyParam.param(new byte[10], ContentType.IMAGE_PNG))
                .isEqualTo("byte[10]");
    }
}
