package core.framework.impl.template;

/**
 * @author neo
 */
public interface FragmentHandler {
    void process(StringBuilder builder, CallStack stack);
}
