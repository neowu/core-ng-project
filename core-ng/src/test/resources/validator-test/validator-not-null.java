public class BeanValidatorNotNullTest$Bean$Validator implements core.framework.impl.validate.BeanValidator {
    private void validateChild1(core.framework.impl.validate.BeanValidatorNotNullTest.Child bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.intField == null) {
            if (!partial) errors.add("child.intField", "intField must not be null");
        } else {
        }
    }

    private void validateChild2(core.framework.impl.validate.BeanValidatorNotNullTest.Child bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.intField == null) {
            if (!partial) errors.add("children.intField", "intField must not be null");
        } else {
        }
    }

    private void validateChild3(core.framework.impl.validate.BeanValidatorNotNullTest.Child bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.intField == null) {
            if (!partial) errors.add("childMap.intField", "intField must not be null");
        } else {
        }
    }

    private void validateBean0(core.framework.impl.validate.BeanValidatorNotNullTest.Bean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.stringField == null) {
            if (!partial) errors.add("stringField", "stringField must not be null");
        } else {
        }
        if (bean.booleanField == null) {
            if (!partial) errors.add("booleanField", "booleanField must not be null");
        } else {
        }
        if (bean.child == null) {
            if (!partial) errors.add("child", "field must not be null");
        } else {
            validateChild1(bean.child, errors, partial);
        }
        if (bean.children == null) {
        } else {
            for (java.util.Iterator iterator = bean.children.iterator(); iterator.hasNext(); ) {
                core.framework.impl.validate.BeanValidatorNotNullTest.Child value = (core.framework.impl.validate.BeanValidatorNotNullTest.Child) iterator.next();
                if (value != null) validateChild2(value, errors, partial);
            }
        }
        if (bean.childMap == null) {
            if (!partial) errors.add("childMap", "field must not be null");
        } else {
            for (java.util.Iterator iterator = bean.childMap.entrySet().iterator(); iterator.hasNext(); ) {
                java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                core.framework.impl.validate.BeanValidatorNotNullTest.Child value = (core.framework.impl.validate.BeanValidatorNotNullTest.Child) entry.getValue();
                if (value != null) validateChild3(value, errors, partial);
            }
        }
    }

    public void validate(Object instance, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.impl.validate.BeanValidatorNotNullTest.Bean) instance, errors, partial);
    }

}
