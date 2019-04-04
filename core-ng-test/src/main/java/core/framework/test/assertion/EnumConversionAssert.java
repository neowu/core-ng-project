package core.framework.test.assertion;

import core.framework.util.Sets;
import org.assertj.core.api.AbstractAssert;

import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public class EnumConversionAssert extends AbstractAssert<EnumConversionAssert, Class<? extends Enum<?>>> {
    public EnumConversionAssert(Class<? extends Enum<?>> actual) {
        super(actual, EnumConversionAssert.class);
    }

    public void hasExactlyConstantsAs(Class<? extends Enum<?>> enumClass) {
        isNotNull();

        Set<String> values1 = enumValues(actual);
        Set<String> values2 = enumValues(enumClass);

        Set<String> diff1 = difference(values1, values2);
        if (!diff1.isEmpty()) failWithMessage("%nExpecting:%n %s%nhas exactly constants of%n %s%nbut some constants were not found:%n <%s>", enumClass.getName(), actual.getName(), diff1);

        Set<String> diff2 = difference(values2, values1);
        if (!diff2.isEmpty()) failWithMessage("%nExpecting:%n %s%nhas exactly constants of%n %s%nbut some constants were not found:%n <%s>", actual.getName(), enumClass.getName(), diff2);
    }

    public void hasAllConstantsOf(Class<? extends Enum<?>> enumClass) {
        isNotNull();

        Set<String> values1 = enumValues(enumClass);
        Set<String> values2 = enumValues(actual);

        Set<String> diff = difference(values1, values2);
        if (!diff.isEmpty()) failWithMessage("%nExpecting:%n %s%nhas all constants of%n %s%nbut some constants were not found:%n <%s>", actual.getName(), enumClass.getName(), diff);
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
        Set<String> values = new HashSet<>(values1);
        values.removeAll(values2);
        return values;
    }
}
