public class BeanValidator$Bean implements core.framework.internal.validate.BeanValidator {
    private void validateBean0(core.framework.internal.validate.BeanValidatorDigitsTest.Bean bean, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        if (bean.num1 == null) {
        } else {
            core.framework.internal.validate.ValidatorHelper.validateDigits(bean.num1, 1, -1, "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", "num1", errors);
        }
        if (bean.num2 == null) {
        } else {
            core.framework.internal.validate.ValidatorHelper.validateDigits(bean.num2, 1, 2, "num2 out of bounds. expected(<{integer} digits>.<{fraction} digits>), actual value={value}", "num2", errors);
        }
        if (bean.num3 == null) {
        } else {
            core.framework.internal.validate.ValidatorHelper.validateDigits(bean.num3, 1, 2, "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", "num3", errors);
        }
        if (bean.num4 == null) {
        } else {
            core.framework.internal.validate.ValidatorHelper.validateDigits(bean.num4, 2, 0, "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}", "num4", errors);
        }
    }

    public void validate(Object instance, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.internal.validate.BeanValidatorDigitsTest.Bean) instance, errors, partial);
    }

}
