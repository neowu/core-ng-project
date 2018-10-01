package core.framework.impl.db;

import core.framework.db.Database;
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
    private final Database database;
    private final Class<T> entityClass;
    private final SelectQuery<T> selectQuery;
    private final StringBuilder whereClause = new StringBuilder();
    private final List<Object> params = Lists.newArrayList();
    private String sort;
    private Integer skip;
    private Integer limit;

    QueryImpl(Database database, Class<T> entityClass, SelectQuery<T> selectQuery) {
        this.database = database;
        this.entityClass = entityClass;
        this.selectQuery = selectQuery;
    }

    @Override
    public void where(String condition, Object... params) {
        if (Strings.isBlank(condition)) throw new Error("condition must not be blank");
        if (whereClause.length() > 0) whereClause.append(" AND ");
        whereClause.append(condition);
        Collections.addAll(this.params, params);
    }

    @Override
    public void orderBy(String sort) {
        this.sort = sort;
    }

    @Override
    public void skip(int skip) {
        this.skip = skip;
    }

    @Override
    public void limit(int limit) {
        this.limit = limit;
    }

    @Override
    public List<T> fetch() {
        if (limit != null && limit == 0) return List.of();  // for pagination search api returns records and count, sometimes it passes limit = 0 to get count only
        String sql = selectQuery.fetchSQL(whereClause, sort, skip, limit);
        Object[] params = selectQuery.fetchParams(this.params, skip, limit);
        return database.select(sql, entityClass, params);
    }

    @Override
    public Optional<T> fetchOne() {
        if (limit != null && limit == 0) return Optional.empty();
        String sql = selectQuery.fetchSQL(whereClause, sort, skip, limit);
        Object[] params = selectQuery.fetchParams(this.params, skip, limit);
        return database.selectOne(sql, entityClass, params);
    }

    @Override
    public <P> Optional<P> project(String projection, Class<P> viewClass) {
        String sql = selectQuery.projectionSQL(projection, whereClause);
        Object[] params = this.params.toArray();
        return database.selectOne(sql, viewClass, params);
    }
}
