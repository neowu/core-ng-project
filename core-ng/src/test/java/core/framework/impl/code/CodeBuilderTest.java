package core.framework.impl.code;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Types;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class CodeBuilderTest {
    @Test
    public void enumVariableLiteral() {
        assertEquals("core.framework.api.http.HTTPMethod.POST", CodeBuilder.enumVariableLiteral(HTTPMethod.POST));
    }

    @Test
    public void typeVariableLiteral() {
        assertEquals("java.lang.String.class", CodeBuilder.typeVariableLiteral(String.class));

        assertEquals("core.framework.api.util.Types.list(java.lang.String.class)", CodeBuilder.typeVariableLiteral(Types.list(String.class)));
    }
}