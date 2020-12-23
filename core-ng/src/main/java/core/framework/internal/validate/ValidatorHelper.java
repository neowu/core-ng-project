package core.framework.internal.validate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author neo
 */
public class ValidatorHelper {  // used by generated BeanValidator
    public static void validateDigits(Number value, int maxIntegerDigits, int maxFractionDigits, String message, String pathLiteral, ValidationErrors errors) {
        BigDecimal number;
        if (value instanceof BigDecimal) {
            number = (BigDecimal) value;
        } else {
            number = new BigDecimal(value.toString()).stripTrailingZeros();
        }
        if (maxIntegerDigits > -1) {
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > maxIntegerDigits) errors.add(pathLiteral, message,
                    Map.of("value", String.valueOf(value),
                            "integer", String.valueOf(maxIntegerDigits),
                            "fraction", maxFractionDigits == -1 ? "inf" : String.valueOf(maxFractionDigits)));
        }
        if (maxFractionDigits > -1) {
            int fractionDigits = Math.max(number.scale(), 0);
            if (fractionDigits > maxFractionDigits) errors.add(pathLiteral, message,
                    Map.of("value", String.valueOf(value),
                            "integer", maxIntegerDigits == -1 ? "inf" : String.valueOf(maxIntegerDigits),
                            "fraction", String.valueOf(maxFractionDigits)));
        }
    }
}
