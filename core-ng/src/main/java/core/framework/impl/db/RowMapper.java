package core.framework.impl.db;

import java.sql.SQLException;

/**
 * @author neo
 */
@FunctionalInterface
interface RowMapper<T> {
    T map(ResultSetWrapper resultSet) throws SQLException;
}
