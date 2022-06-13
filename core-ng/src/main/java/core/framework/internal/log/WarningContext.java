package core.framework.internal.log;

import core.framework.db.DBWarning;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public final class WarningContext {    // use static typing for performance
    public boolean suppressSlowSQLWarning;
    // max db calls per action, if exceeds, it indicates bad practice (not CD friendly), better split into multiple actions
    public int maxDBOperations = 2000;
    public int maxDBRows = 2000;

    public void initialize(@Nullable DBWarning warning) {
        if (warning != null) {
            if (warning.maxOperations() > 0) maxDBOperations = warning.maxOperations();
            if (warning.maxRows() > 0) maxDBRows = warning.maxRows();
        }
    }

    public void initialize(WarningContext parentContext) {
        maxDBOperations = parentContext.maxDBOperations;
        maxDBRows = parentContext.maxDBRows;
    }
}
