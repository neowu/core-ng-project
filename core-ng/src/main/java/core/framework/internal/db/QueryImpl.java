package core.framework.internal.db;

import core.framework.db.Query;
import core.framework.util.Lists;
import core.framework.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class QueryImpl<T> implements Query<T> {
    private final DatabaseImpl database;
    private final Class<T> entityClass;
    private final SelectQuery<T> selectQuery;
    private final StringBuilder whereClause = new StringBuilder();
    private final List<Object> params = Lists.newArrayList();
    private String groupBy;
    private String sort;
    private Integer skip;
    private Integer limit;

    QueryImpl(DatabaseImpl database, Class<T> entityClass, SelectQuery<T> selectQuery) {
        this.database = database;
        this.entityClass = entityClass;
        this.selectQuery = selectQuery;
    }

    @Override
    public void where(String condition, Object... params) {
        if (Strings.isBlank(condition)) throw new Error("condition must not be blank");
        if (!whereClause.isEmpty()) whereClause.append(" AND ");
        if (condition.contains(" OR ") || condition.contains(" or ")) { // fastest way to only cover common cases, ignore Or or oR
            whereClause.append('(').append(condition).append(')');
        } else {
            whereClause.append(condition);
        }
        Collections.addAll(this.params, params);
    }

    @Override
    public void orderBy(String sort) {
        this.sort = sort;
    }

    @Override
    public void groupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    @Override
    public void skip(Integer skip) {
        this.skip = skip;
    }

    @Override
    public void limit(Integer limit) {
        if (limit != null && limit <= 0) throw new Error("limit must be greater than 0, limit=" + limit);
        this.limit = limit;
    }

    @Override
    public List<T> fetch() {
        if (groupBy != null) throw new Error("fetch must not be used with groupBy, groupBy=" + groupBy);
        String sql = selectQuery.fetchSQL(whereClause, sort, skip, limit);
        Object[] params = selectQuery.params(this.params, skip, limit);
        return database.select(sql, entityClass, params);
    }

    @Override
    public Optional<T> fetchOne() {
        if (groupBy != null) throw new Error("fetch must not be used with groupBy, groupBy=" + groupBy);
        String sql = selectQuery.fetchSQL(whereClause, sort, skip, limit);
        Object[] params = selectQuery.params(this.params, skip, limit);
        return database.selectOne(sql, entityClass, params);
    }

    @Override
    public <P> List<P> project(String projection, Class<P> viewClass) {
        String sql = selectQuery.sql(projection, whereClause, groupBy, sort, skip, limit);
        Object[] params = selectQuery.params(this.params, skip, limit);
        return database.select(sql, viewClass, params);
    }

    @Override
    public <P> Optional<P> projectOne(String projection, Class<P> viewClass) {
        String sql = selectQuery.sql(projection, whereClause, groupBy, sort, skip, limit);
        Object[] params = selectQuery.params(this.params, skip, limit);
        return database.selectOne(sql, viewClass, params);
    }
}
