public class BeanValidatorNotNullTest$Bean$Validator implements core.framework.impl.validate.BeanValidator {
    private void validateChildBean1(core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.intField == null) {
            if (!partial) errors.add("child.intField", "intField must not be null");
        } else {
        }
    }

    private void validateChildBean2(core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.intField == null) {
            if (!partial) errors.add("children.intField", "intField must not be null");
        } else {
        }
    }

    private void validateChildBean3(core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
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
            validateChildBean1(bean.child, errors, partial);
        }
        if (bean.children == null) {
        } else {
            for (java.util.Iterator iterator = bean.children.iterator(); iterator.hasNext(); ) {
                core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean value = (core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean) iterator.next();
                if (value != null) validateChildBean2(value, errors, partial);
            }
        }
        if (bean.childMap == null) {
            if (!partial) errors.add("childMap", "field must not be null");
        } else {
            for (java.util.Iterator iterator = bean.childMap.entrySet().iterator(); iterator.hasNext(); ) {
                java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean value = (core.framework.impl.validate.BeanValidatorNotNullTest.ChildBean) entry.getValue();
                if (value != null) validateChildBean3(value, errors, partial);
            }
        }
    }

    public void validate(Object instance, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        validateBean0((core.framework.impl.validate.BeanValidatorNotNullTest.Bean) instance, errors, partial);
    }

}
