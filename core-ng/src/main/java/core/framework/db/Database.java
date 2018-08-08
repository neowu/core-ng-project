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

    int[] batchExecute(String sql, List<Object[]> params);

    Transaction beginTransaction();
}
