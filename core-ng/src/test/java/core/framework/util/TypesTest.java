package core.framework.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypesTest {
    @SuppressFBWarnings("UUF_UNUSED_FIELD")
    List<String> list;

    @Test
    void equalsToBuiltInType() throws NoSuchFieldException {
        Type builtInType = TypesTest.class.getDeclaredField("list").getGenericType();

        Type constructedType = Types.generic(List.class, String.class);

        assertThat(constructedType).isEqualTo(builtInType)
                                   .hasSameHashCodeAs(builtInType);
        assertThat(builtInType).isEqualTo(constructedType)
                               .hasSameHashCodeAs(constructedType);
    }
}
