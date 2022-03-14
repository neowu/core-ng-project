package core.framework.internal.log;

/**
 * @author neo
 */
public final class InternalActionContext {    // use static typing for performance
    public boolean suppressSlowSQLWarning;
    public int maxDBOperations;
}
