package core.framework.impl.template;

/**
 * @author neo
 */
public interface CompositeHandler extends FragmentHandler {
    void add(FragmentHandler handler);
}
