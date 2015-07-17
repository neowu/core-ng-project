package core.framework.impl.template.function;

/**
 * @author neo
 */
public interface Function {
    default Object apply(Object param) {
        return apply(new Object[]{param});
    }

    default Object apply(Object param1, Object param2) {
        return apply(new Object[]{param1, param2});
    }

    default Object apply(Object param1, Object param2, Object param3) {
        return apply(new Object[]{param1, param2, param3});
    }

    Object apply(Object[] params);
}
