public class BeanValidatorDigitsTest$Bean$Validator implements core.framework.internal.validate.BeanValidator {
    private void validateBean0(core.framework.internal.validate.BeanValidatorDigitsTest.Bean bean, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        if (bean.num1 == null) {
        } else {
            java.math.BigDecimal number;
            if ((java.lang.Number) bean.num1 instanceof java.math.BigDecimal) number = (java.math.BigDecimal) ((java.lang.Number) bean.num1);
            else number = new java.math.BigDecimal(bean.num1.toString()).stripTrailingZeros();
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > 1) errors.add("num1", "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", java.util.Map.of("value", String.valueOf(bean.num1), "integer", "1", "fraction", "inf"));
        }
        if (bean.num2 == null) {
        } else {
            java.math.BigDecimal number;
            if ((java.lang.Number) bean.num2 instanceof java.math.BigDecimal) number = (java.math.BigDecimal) ((java.lang.Number) bean.num2);
            else number = new java.math.BigDecimal(bean.num2.toString()).stripTrailingZeros();
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > 1) errors.add("num2", "num2 out of bounds. expected(<{integer} digits>.<{fraction} digits>), actual value={value}", java.util.Map.of("value", String.valueOf(bean.num2), "integer", "1", "fraction", "2"));
            int fractionDigits = number.scale() < 0 ? 0 : number.scale();
            if (fractionDigits > 2) errors.add("num2", "num2 out of bounds. expected(<{integer} digits>.<{fraction} digits>), actual value={value}", java.util.Map.of("value", String.valueOf(bean.num2), "integer", "1", "fraction", "2"));
        }
        if (bean.num3 == null) {
        } else {
            java.math.BigDecimal number;
            if ((java.lang.Number) bean.num3 instanceof java.math.BigDecimal) number = (java.math.BigDecimal) ((java.lang.Number) bean.num3);
            else number = new java.math.BigDecimal(bean.num3.toString()).stripTrailingZeros();
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > 1) errors.add("num3", "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", java.util.Map.of("value", String.valueOf(bean.num3), "integer", "1", "fraction", "2"));
            int fractionDigits = number.scale() < 0 ? 0 : number.scale();
            if (fractionDigits > 2) errors.add("num3", "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", java.util.Map.of("value", String.valueOf(bean.num3), "integer", "1", "fraction", "2"));
        }
        if (bean.num4 == null) {
        } else {
            java.math.BigDecimal number;
            if ((java.lang.Number) bean.num4 instanceof java.math.BigDecimal) number = (java.math.BigDecimal) ((java.lang.Number) bean.num4);
            else number = new java.math.BigDecimal(bean.num4.toString()).stripTrailingZeros();
            int integerDigits = number.precision() - number.scale();
            if (integerDigits > 2) errors.add("num4", "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", java.util.Map.of("value", String.valueOf(bean.num4), "integer", "2", "fraction", "0"));
            int fractionDigits = number.scale() < 0 ? 0 : number.scale();
            if (fractionDigits > 0) errors.add("num4", "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", java.util.Map.of("value", String.valueOf(bean.num4), "integer", "2", "fraction", "0"));
        }
    }

    public void validate(Object instance, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.internal.validate.BeanValidatorDigitsTest.Bean) instance, errors, partial);
    }

}
