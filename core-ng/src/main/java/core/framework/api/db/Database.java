package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Database {
    @Deprecated
    <T> List<T> select(String sql, RowMapper<T> mapper, Object... params);

    <T> List<T> select(String sql, Class<T> viewClass, Object... params);

    @Deprecated
    <T> Optional<T> selectOne(String sql, RowMapper<T> mapper, Object... params);

    <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params);

    default Optional<String> selectString(String sql, Object... params) {
        return selectOne(sql, row -> row.getString(1), params);
    }

    default Optional<Integer> selectInt(String sql, Object... params) {
        return selectOne(sql, row -> row.getInt(1), params);
    }

    default Optional<Long> selectLong(String sql, Object... params) {
        return selectOne(sql, row -> row.getLong(1), params);
    }

    int execute(String sql, Object... params);

    Transaction beginTransaction();
}
