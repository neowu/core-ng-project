package core.framework.impl.db;

import java.sql.SQLException;

/**
 * @author neo
 */
interface RowMapper<T> {
    T map(ResultSetWrapper resultSet) throws SQLException;
}
