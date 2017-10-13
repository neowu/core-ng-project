package core.framework.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

public class TypesTest {
    List<String> list;

    @Test
    public void equalsToBuiltInType() throws NoSuchFieldException {
        Type builtInType = TypesTest.class.getDeclaredField("list").getGenericType();

        Type constructedType = Types.generic(List.class, String.class);

        Assert.assertEquals(constructedType, builtInType);
        Assert.assertEquals(builtInType, constructedType);

        Assert.assertEquals(constructedType.hashCode(), builtInType.hashCode());
    }
}
