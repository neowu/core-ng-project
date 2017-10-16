package core.framework.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypesTest {
    List<String> list;

    @Test
    void equalsToBuiltInType() throws NoSuchFieldException {
        Type builtInType = TypesTest.class.getDeclaredField("list").getGenericType();

        Type constructedType = Types.generic(List.class, String.class);

        assertEquals(constructedType, builtInType);
        assertEquals(builtInType, constructedType);

        assertEquals(constructedType.hashCode(), builtInType.hashCode());
    }
}
