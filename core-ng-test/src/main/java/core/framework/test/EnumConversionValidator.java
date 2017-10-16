package core.framework.test;

import core.framework.util.Sets;
import core.framework.util.Strings;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * usually there is need to convert WS interface enum to domain enum and vise versa, this class is to validate 2 enum has consistent enum names
 *
 * @author neo
 */
public class EnumConversionValidator {
    public void validate(Class<? extends Enum<?>> enumClass1, Class<? extends Enum<?>> enumClass2) {
        assertNotEquals(enumClass1, enumClass2);
        assertTrue(enumClass1.isEnum());
        assertTrue(enumClass2.isEnum());

        Set<String> values1 = enumValues(enumClass1);
        Set<String> values2 = enumValues(enumClass2);

        Set<String> diff1 = difference(values1, values2);
        assertTrue(diff1.isEmpty(), Strings.format("enum values from {} can not be converted to {}, values={}", enumClass1.getSimpleName(), enumClass2.getSimpleName(), diff1));

        Set<String> diff2 = difference(values2, values1);
        assertTrue(diff2.isEmpty(), Strings.format("enum values from {} can not be converted to {}, values={}", enumClass2.getSimpleName(), enumClass1.getSimpleName(), diff2));
    }

    private Set<String> enumValues(Class<? extends Enum<?>> enumClass1) {
        Set<String> values = Sets.newHashSet();
        Enum<?>[] constants = enumClass1.getEnumConstants();
        for (Enum<?> constant : constants) {
            values.add(constant.name());
        }
        return values;
    }

    private Set<String> difference(Set<String> values1, Set<String> values2) {
        HashSet<String> values = new HashSet<>(values1);
        values.removeAll(values2);
        return values;
    }
}
