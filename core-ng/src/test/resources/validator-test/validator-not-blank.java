public class BeanValidator$Bean implements core.framework.internal.validate.BeanValidator {
    private void validateBean0(core.framework.internal.validate.BeanValidatorNotBlankTest.Bean bean, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        if (bean.stringField1 == null) {
            if (!partial) errors.add("stringField1", "field must not be null", null);
        } else {
            if (bean.stringField1.isBlank()) errors.add("stringField1", "stringField1 must not be blank", null);
        }
        if (bean.stringField2 == null) {
        } else {
            if (bean.stringField2.isBlank()) errors.add("stringField2", "stringField2 must not be blank", null);
        }
    }

    public void validate(Object instance, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.internal.validate.BeanValidatorNotBlankTest.Bean) instance, errors, partial);
    }

}
