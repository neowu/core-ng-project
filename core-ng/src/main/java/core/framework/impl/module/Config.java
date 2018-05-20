package core.framework.impl.module;

/**
 * @author neo
 */
public abstract class Config {  // use abstract class not interface is for hiding those method from app module
    protected abstract void initialize(ModuleContext context, String name);

    protected abstract void validate();
}
