package core.framework.internal.validate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author neo
 */
public class ValidatorHelper {  // used by generated BeanValidator
    public static void validateDigits(Number value, int integer, int fraction, String message, String pathLiteral, ValidationErrors errors) {
        BigDecimal number;
        if (value instanceof BigDecimal) {
            number = (BigDecimal) value;
        } else {
            number = new BigDecimal(value.toString()).stripTrailingZeros();
        }
        if (integer > -1) {
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > integer) errors.add(pathLiteral, message,
                    Map.of("value", String.valueOf(value),
                            "integer", String.valueOf(integer),
                            "fraction", fraction == -1 ? "inf" : String.valueOf(fraction)));
        }
        if (fraction > -1) {
            int fractionDigits = Math.max(number.scale(), 0);
            if (fractionDigits > fraction) errors.add(pathLiteral, message,
                    Map.of("value", String.valueOf(value),
                            "integer", integer == -1 ? "inf" : String.valueOf(integer),
                            "fraction", String.valueOf(fraction)));
        }
    }
}
