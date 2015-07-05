package core.framework.api.db;

/**
 * @author neo
 */
public interface RowMapper<T> {
    T map(Row row);
}
