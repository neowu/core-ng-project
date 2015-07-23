package core.framework.impl.template.function;

/**
 * @author neo
 */
public class HTMLFunction implements Function {
    @Override
    public Object apply(Object[] params) {
        return new HTMLText((String) params[0]);
    }
}
