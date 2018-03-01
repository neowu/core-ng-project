package core.framework.util;

import java.math.BigDecimal;

/**
 * @author rexthk
 */
public final class BigDecimals {
    private static final int ZERO = 0;

    public static BigDecimals is(BigDecimal decimal) {
        return new BigDecimals(decimal);
    }

    private final BigDecimal decimal;

    private BigDecimals(BigDecimal decimal) {
        this.decimal = decimal;
    }

    public boolean isGreaterThan(BigDecimal decimal) {
        return this.decimal.compareTo(decimal) > ZERO;
    }

    public boolean isSmallerThan(BigDecimal decimal) {
        return this.decimal.compareTo(decimal) < ZERO;
    }

    public boolean isEqualTo(BigDecimal decimal) {
        return this.decimal.compareTo(decimal) == ZERO;
    }
}
