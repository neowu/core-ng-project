public class BeanValidator$Bean implements core.framework.internal.validate.BeanValidator {
    private void validateBean0(core.framework.internal.validate.BeanValidatorStringLengthTest.Bean bean, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        if (bean.field1 == null) {
            if (!partial) errors.add("field1", "field must not be null", null);
        } else {
            if (bean.field1.length() > 5) errors.add("field1", "field1 must not be longer than {max}", java.util.Map.of("value", String.valueOf(bean.field1.length()), "min", "0", "max", "5"));
        }
        if (bean.field2 == null) {
            if (!partial) errors.add("field2", "field must not be null", null);
        } else {
            if (bean.field2.length() < 5) errors.add("field2", "field2 must be longer than {min}", java.util.Map.of("value", String.valueOf(bean.field2.length()), "min", "5", "max", "inf"));
        }
        if (bean.field3 == null) {
        } else {
            if (bean.field3.length() < 3) errors.add("field3", "size must be between {min} and {max}, size={value}", java.util.Map.of("value", String.valueOf(bean.field3.length()), "min", "3", "max", "5"));
            if (bean.field3.length() > 5) errors.add("field3", "size must be between {min} and {max}, size={value}", java.util.Map.of("value", String.valueOf(bean.field3.length()), "min", "3", "max", "5"));
        }
    }

    public void validate(Object instance, core.framework.internal.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.internal.validate.BeanValidatorStringLengthTest.Bean) instance, errors, partial);
    }

}
