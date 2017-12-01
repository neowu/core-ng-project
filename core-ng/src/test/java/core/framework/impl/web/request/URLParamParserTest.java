package core.framework.impl.web.request;

import core.framework.api.json.Property;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class URLParamParserTest {
    @Test
    void failedToParseEnum() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> URLParamParser.parse("V2", TestEnum.class));
        assertThat(exception.getMessage()).contains("failed to parse");
    }

    @Test
    void parseBoolean() {
        assertTrue(URLParamParser.parse("true", Boolean.class));
    }

    @Test
    void parseEnum() {
        TestEnum value = URLParamParser.parse("V1", TestEnum.class);
        assertEquals(TestEnum.VALUE, value);
    }

    enum TestEnum {
        @Property(name = "V1")
        VALUE
    }
}
