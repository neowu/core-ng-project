package core.framework.impl.template;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author neo
 */
public class TemplateContext {
    private final Deque<Object> stack = new LinkedList<>();

    public TemplateContext(Object root) {
        stack.add(root);
    }

    public Object eval(String expression) {
        for (Object value : stack) {
            if (value instanceof Map) return ((Map) value).get(expression);
            if (expression.equals(".")) return value;
        }
        return null;
    }

    public void push(Object value) {
        stack.push(value);
    }

    public void pop() {
        stack.pop();
    }
}
