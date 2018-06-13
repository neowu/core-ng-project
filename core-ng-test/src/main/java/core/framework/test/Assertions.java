package core.framework.test;

import core.framework.test.assertion.EnumConversionAssert;
import core.framework.test.assertion.EnvResourceAssert;
import core.framework.test.assertion.ValidatorAssert;

/**
 * @author neo
 */
public class Assertions {
    public static EnumConversionAssert assertThat(Class<? extends Enum<?>> enumClass) {
        return new EnumConversionAssert(enumClass);
    }

    public static ValidatorAssert assertThat(Object bean) {
        return new ValidatorAssert(bean);
    }

    public static EnvResourceAssert assertConf() {
        return new EnvResourceAssert();
    }
}
