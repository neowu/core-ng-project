package core.framework.internal.module;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public abstract class Config {  // use abstract class not interface is for hiding those method from app module, interface methods must be public
    protected abstract void initialize(ModuleContext context, @Nullable String name);

    // after initialization done, validate and finalize
    protected void validate() {
    }
}
