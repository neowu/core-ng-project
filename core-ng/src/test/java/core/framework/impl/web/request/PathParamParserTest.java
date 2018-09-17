package core.framework.impl.web.request;

import core.framework.api.json.Property;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathParamParserTest {
    @Test
    void failedToParseEnum() {
        assertThatThrownBy(() -> PathParamParser.parse("V2", TestEnum.class))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse enum");
    }

    @Test
    void parseInteger() {
        assertThat(PathParamParser.parse("100", Integer.class)).isEqualTo(100);
    }

    @Test
    void parseEnum() {
        assertThat(PathParamParser.parse("V1", TestEnum.class))
                .isEqualTo(TestEnum.VALUE);
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
