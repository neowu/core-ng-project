package core.framework.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Database {
    <T> List<T> select(String sql, Class<T> viewClass, Object... params);

    <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params);

    int execute(String sql, Object... params);

    // for bulk update operations, you may want to enclose it with Transaction to improve performance
    // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executePreparedBatchAsMultiStatement, mysql driver simply sends multiple queries with ';' as one statement,
    // so it will reduce cost of creating transaction for each statement
    int[] batchExecute(String sql, List<Object[]> params);

    Transaction beginTransaction();
}
