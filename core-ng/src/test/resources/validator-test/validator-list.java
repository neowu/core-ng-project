public class ObjectValidatorListTest$Bean$ObjectValidator implements core.framework.impl.validate.ObjectValidator {
    private void validateChildBean1(core.framework.impl.validate.ObjectValidatorListTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.field3 == null) {
            if (!partial) errors.add("child.field3", "field3 must not be null");
        } else {
            if (core.framework.util.Strings.isEmpty(bean.field3)) errors.add("child.field3", "field3 must not be empty");
        }
    }

    private void validateChildBean2(core.framework.impl.validate.ObjectValidatorListTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.field3 == null) {
            if (!partial) errors.add("children.field3", "field3 must not be null");
        } else {
            if (core.framework.util.Strings.isEmpty(bean.field3)) errors.add("children.field3", "field3 must not be empty");
        }
    }

    private void validateChildBean3(core.framework.impl.validate.ObjectValidatorListTest.ChildBean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.field3 == null) {
            if (!partial) errors.add("childMap.field3", "field3 must not be null");
        } else {
            if (core.framework.util.Strings.isEmpty(bean.field3)) errors.add("childMap.field3", "field3 must not be empty");
        }
    }

    private void validateBean0(core.framework.impl.validate.ObjectValidatorListTest.Bean bean, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        if (bean.field1 == null) {
            if (!partial) errors.add("field1", "field must not be null");
        } else {
            if (core.framework.util.Strings.isEmpty(bean.field1)) errors.add("field1", "field1 must not be empty");
        }
        if (bean.field2 == null) {
        } else {
            if (core.framework.util.Strings.isEmpty(bean.field2)) errors.add("field2", "field2 must not be empty");
        }
        if (bean.child == null) {
        } else {
            validateChildBean1(bean.child, errors, partial);
        }
        if (bean.children == null) {
        } else {
            for (java.util.Iterator iterator = bean.children.iterator(); iterator.hasNext(); ) {
                core.framework.impl.validate.ObjectValidatorListTest.ChildBean value = (core.framework.impl.validate.ObjectValidatorListTest.ChildBean) iterator.next();
                if (value != null) validateChildBean2(value, errors, partial);
            }
        }
        if (bean.childMap == null) {
        } else {
            for (java.util.Iterator iterator = bean.childMap.entrySet().iterator(); iterator.hasNext(); ) {
                java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
                core.framework.impl.validate.ObjectValidatorListTest.ChildBean value = (core.framework.impl.validate.ObjectValidatorListTest.ChildBean) entry.getValue();
                if (value != null) validateChildBean3(value, errors, partial);
            }
        }
    }

    public void validate(Object instance, core.framework.impl.validate.ValidationErrors errors, boolean partial) {
        java.util.List list = (java.util.List) instance;
        for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            core.framework.impl.validate.ObjectValidatorListTest.Bean value = (core.framework.impl.validate.ObjectValidatorListTest.Bean) iterator.next();
            if (value != null) validateBean0(value, errors, partial);
        }
    }

}
