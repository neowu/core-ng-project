package core.framework.impl.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesLogParamTest {
    @Test
    void convertToString() {
        assertThat(new BytesLogParam(Strings.bytes("value")).toString()).isEqualTo("value");

        assertThat(new BytesLogParam(null).toString()).isEqualTo("null");
    }
}
