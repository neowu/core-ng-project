package core.framework.impl.db.dialect;

import java.util.List;

/**
 * @author neo
 */
public interface Dialect {
    String fetchSQL(StringBuilder whereClause, String sort, Integer skip, Integer limit);

    Object[] fetchParams(List<Object> params, Integer skip, Integer limit);
}
