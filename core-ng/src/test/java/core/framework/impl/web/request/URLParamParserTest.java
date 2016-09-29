package core.framework.impl.web.request;

import core.framework.api.web.exception.BadRequestException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.bind.annotation.XmlEnumValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class URLParamParserTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void failedToParseEnum() {
        exception.expect(BadRequestException.class);
        exception.expectMessage("failed to parse");

        URLParamParser.parse("V2", TestEnum.class);
    }

    @Test
    public void parseBoolean() {
        assertTrue(URLParamParser.parse("true", Boolean.class));
    }

    @Test
    public void parseEnum() {
        TestEnum value = URLParamParser.parse("V1", TestEnum.class);
        assertEquals(TestEnum.VALUE, value);
    }

    enum TestEnum {
        @XmlEnumValue("V1")
        VALUE
    }
}