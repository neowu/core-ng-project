public class ObjectValidatorLengthTest$Bean$ObjectValidator implements core.framework.impl.validate.ObjectValidator {
    private void validateBean0(core.framework.impl.validate.ObjectValidatorLengthTest.Bean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.field1 == null) {
            if (!partial) errors.add("field1", "field must not be null");
        } else {
            if (bean.field1.length() > 5) errors.add("field1", "field1 must not be longer than 5");
        }
        if (bean.field2 == null) {
            if (!partial) errors.add("field2", "field must not be null");
        } else {
            if (bean.field2.length() < 5) errors.add("field2", "field2 must be longer than 5");
        }
        if (bean.field3 == null) {
        } else {
            if (bean.field3.length() < 3) errors.add("field3", "field3 length must between 3 and 5");
            if (bean.field3.length() > 5) errors.add("field3", "field3 length must between 3 and 5");
        }
    }

    public void validate(Object instance, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.impl.validate.ObjectValidatorLengthTest.Bean) instance, errors, partial);
    }

}
