package core.framework.test;

import org.junit.Test;

/**
 * @author neo
 */
public class EnumConversionValidatorTest {
    @Test
    public void validate() {
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
