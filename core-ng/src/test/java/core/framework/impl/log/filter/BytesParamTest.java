package core.framework.impl.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesParamTest {
    @Test
    void convertToString() {
        assertThat(new BytesParam(Strings.bytes("value")).toString()).isEqualTo("value");
    }
}
