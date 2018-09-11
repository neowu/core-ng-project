package core.framework.impl.web.request;

import core.framework.api.json.Property;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class URLParamParserTest {
    @Test
    void failedToParseEnum() {
        assertThatThrownBy(() -> URLParamParser.parse("V2", TestEnum.class))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse enum");
    }

    @Test
    void parseBoolean() {
        assertThat(URLParamParser.parse("true", Boolean.class)).isTrue();
    }

    @Test
    void parseEnum() {
        assertThat(URLParamParser.parse("V1", TestEnum.class))
                .isEqualTo(TestEnum.VALUE);
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
