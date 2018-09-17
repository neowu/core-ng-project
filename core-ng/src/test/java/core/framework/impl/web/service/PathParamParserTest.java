package core.framework.impl.web.service;

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
    void toInt() {
        assertThat(PathParamParser.toInt("100")).isEqualTo(100);

        assertThatThrownBy(() -> PathParamParser.toInt("X"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse int");
    }

    @Test
    void toLong() {
        assertThat(PathParamParser.toLong("100")).isEqualTo(100);

        assertThatThrownBy(() -> PathParamParser.toLong("X"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse long");
    }

    @Test
    void toEnum() {
        assertThat(PathParamParser.toEnum("V1", TestEnum.class)).isEqualTo(TestEnum.VALUE);

        assertThatThrownBy(() -> PathParamParser.toEnum("V2", TestEnum.class))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse enum");
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
