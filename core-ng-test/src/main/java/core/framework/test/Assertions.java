package core.framework.test;

import core.framework.test.assertion.EnumConversionAssert;
import core.framework.test.assertion.EnvResourceAssert;
import core.framework.test.assertion.ValidatorAssert;

/**
 * @author neo
 */
public class Assertions {
    public static EnumConversionAssert assertEnumClass(Class<? extends Enum<?>> enumClass) {
        return new EnumConversionAssert(enumClass);
    }

    public static ValidatorAssert assertBean(Object bean) {
        return new ValidatorAssert(bean);
    }

    public static EnvResourceAssert assertConfDirectory() {
        return new EnvResourceAssert();
    }
}
