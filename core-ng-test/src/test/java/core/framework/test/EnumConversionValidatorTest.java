package core.framework.test;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class EnumConversionValidatorTest {
    @Test
    void validate() {
        new EnumConversionValidator().validate(Enum1.class, Enum2.class);
    }

    public enum Enum1 {
        A,
        B
    }

    public enum Enum2 {
        A,
        B
    }
}
