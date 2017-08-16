package core.framework.impl.db.dialect;

import java.util.List;

/**
 * @author neo
 */
public interface Dialect {
    String fetchSQL(String where, String sort, Integer skip, Integer limit);

    Object[] fetchParams(List<Object> params, Integer skip, Integer limit);
}
