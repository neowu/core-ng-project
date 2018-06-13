package core.framework.test.assertion;

/**
 * @author neo
 */
public class Assertions {
    public static EnumConversionAssertion assertThat(Class<? extends Enum<?>> enumClass) {
        return new EnumConversionAssertion(enumClass);
    }

    public static ValidatorAssertion assertThat(Object bean) {
        return new ValidatorAssertion(bean);
    }
}
