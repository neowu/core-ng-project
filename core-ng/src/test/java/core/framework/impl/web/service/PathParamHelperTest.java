package core.framework.impl.web.service;

import core.framework.api.json.Property;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathParamHelperTest {
    @Test
    void parseInt() {
        assertThat(PathParamHelper.toInt("100")).isEqualTo(100);

        assertThatThrownBy(() -> PathParamHelper.toInt("X"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse int");
    }

    @Test
    void parseLong() {
        assertThat(PathParamHelper.toLong("100")).isEqualTo(100);

        assertThatThrownBy(() -> PathParamHelper.toLong("X"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse long");
    }

    @Test
    void parseEnum() {
        assertThat(PathParamHelper.toEnum("V1", TestEnum.class)).isEqualTo(TestEnum.VALUE);

        assertThatThrownBy(() -> PathParamHelper.toEnum("V2", TestEnum.class))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("failed to parse enum");
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
