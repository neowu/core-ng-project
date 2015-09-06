package core.framework.impl.db;

import core.framework.api.util.Exceptions;

import java.sql.SQLException;

/**
 * @author neo
 */
public final class ScalarRowMappers {
    final StringRowMapper singleString = new StringRowMapper();
    final IntRowMapper singleInt = new IntRowMapper();
    final LongRowMapper singleLong = new LongRowMapper();

    public static class StringRowMapper implements RowMapper<String> {
        @Override
        public String map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getString(1);
        }
    }

    public static class IntRowMapper implements RowMapper<Integer> {
        @Override
        public Integer map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getInt(1);
        }
    }

    public static class LongRowMapper implements RowMapper<Long> {
        @Override
        public Long map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getLong(1);
        }
    }

    private static void checkColumnCount(ResultSetWrapper resultSet) {
        int count = resultSet.columnCount();
        if (count > 1) throw Exceptions.error("returned column count must be one, count={}", count);
    }
}
