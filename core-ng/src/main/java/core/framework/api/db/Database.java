package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Database {
    <T> List<T> select(String sql, Class<T> viewClass, Object... params);

    <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params);

    Optional<String> selectString(String sql, Object... params);

    Optional<Integer> selectInt(String sql, Object... params);

    Optional<Long> selectLong(String sql, Object... params);

    int execute(String sql, Object... params);

    Transaction beginTransaction();
}
