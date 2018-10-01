public class BeanValidatorNotBlankTest$Bean$Validator implements core.framework.impl.validate.BeanValidator {
    private void validateBean0(core.framework.impl.validate.BeanValidatorNotBlankTest.Bean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.stringField1 == null) {
            if (!partial) errors.add("stringField1", "field must not be null");
        } else {
            if (bean.stringField1.isBlank()) errors.add("stringField1", "stringField1 must not be blank");
        }
        if (bean.stringField2 == null) {
        } else {
            if (bean.stringField2.isBlank()) errors.add("stringField2", "stringField2 must not be blank");
        }
    }

    public void validate(Object instance, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.impl.validate.BeanValidatorNotBlankTest.Bean) instance, errors, partial);
    }

}
