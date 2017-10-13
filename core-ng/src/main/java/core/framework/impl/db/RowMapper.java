package core.framework.impl.db;

import core.framework.util.Exceptions;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
@FunctionalInterface
interface RowMapper<T> {
    static void checkColumnCount(ResultSetWrapper resultSet) {
        int count = resultSet.columnCount();
        if (count > 1) throw Exceptions.error("returned column count must be one, count={}", count);
    }

    T map(ResultSetWrapper resultSet) throws SQLException;

    class StringRowMapper implements RowMapper<String> {
        @Override
        public String map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getString(1);
        }
    }

    class IntegerRowMapper implements RowMapper<Integer> {
        @Override
        public Integer map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getInt(1);
        }
    }

    class LongRowMapper implements RowMapper<Long> {
        @Override
        public Long map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getLong(1);
        }
    }

    class DoubleRowMapper implements RowMapper<Double> {
        @Override
        public Double map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getDouble(1);
        }
    }

    class BigDecimalRowMapper implements RowMapper<BigDecimal> {
        @Override
        public BigDecimal map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getBigDecimal(1);
        }
    }

    class LocalDateTimeRowMapper implements RowMapper<LocalDateTime> {
        @Override
        public LocalDateTime map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getLocalDateTime(1);
        }
    }

    class LocalDateRowMapper implements RowMapper<LocalDate> {
        @Override
        public LocalDate map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getLocalDate(1);
        }
    }

    class ZonedDateTimeRowMapper implements RowMapper<ZonedDateTime> {
        @Override
        public ZonedDateTime map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getZonedDateTime(1);
        }
    }

    class BooleanRowMapper implements RowMapper<Boolean> {
        @Override
        public Boolean map(ResultSetWrapper resultSet) throws SQLException {
            checkColumnCount(resultSet);
            return resultSet.getBoolean(1);
        }
    }
}
