package core.framework.test;

import core.framework.api.util.Strings;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class EnumConversionValidator {
    public void validate(Class<? extends Enum<?>> enumClass1, Class<? extends Enum<?>> enumClass2) {
        assertNotEquals(enumClass1, enumClass2);
        assertTrue(enumClass1.isEnum());
        assertTrue(enumClass2.isEnum());

        Set<String> values1 = Stream.of(enumClass1.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());
        Set<String> values2 = Stream.of(enumClass2.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());

        Set<String> diff1 = difference(values1, values2);
        assertTrue(Strings.format("enum values from {} can not be converted to {}, values={}", enumClass1.getSimpleName(), enumClass2.getSimpleName(), diff1), diff1.isEmpty());

        Set<String> diff2 = difference(values2, values1);
        assertTrue(Strings.format("enum values from {} can not be converted to {}, values={}", enumClass2.getSimpleName(), enumClass1.getSimpleName(), diff2), diff2.isEmpty());
    }

    private Set<String> difference(Set<String> values1, Set<String> values2) {
        HashSet<String> values = new HashSet<>(values1);
        values.removeAll(values2);
        return values;
    }
}
