package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Database {
    <T> List<T> select(Query query, RowMapper<T> mapper);

    <T> List<T> select(Query query, Class<T> viewClass);

    <T> Optional<T> selectOne(Query query, RowMapper<T> mapper);

    <T> Optional<T> selectOne(Query query, Class<T> viewClass);

    Optional<String> selectString(String sql, Object... params);

    Optional<Integer> selectInt(String sql, Object... params);

    Optional<Long> selectLong(String sql, Object... params);

    int execute(Query query);

    int execute(String sql, Object... params);

    Transaction beginTransaction();
}
