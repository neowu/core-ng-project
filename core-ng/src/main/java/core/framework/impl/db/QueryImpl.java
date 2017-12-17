package core.framework.impl.db;

import core.framework.db.Query;
import core.framework.impl.db.dialect.Dialect;
import core.framework.util.Exceptions;
import core.framework.util.Lists;
import core.framework.util.Strings;

import java.util.Collections;
import java.util.List;

/**
 * @author neo
 */
public class QueryImpl<T> implements Query<T> {
    private final RepositoryImpl<T> repository;
    private final Dialect dialect;
    private final StringBuilder whereClause = new StringBuilder();
    private final List<Object> params = Lists.newArrayList();
    private String sort;
    private Integer skip;
    private Integer limit;

    QueryImpl(RepositoryImpl<T> repository, Dialect dialect) {
        this.repository = repository;
        this.dialect = dialect;
    }

    @Override
    public Query<T> where(String condition, Object... params) {
        if (Strings.isEmpty(condition)) throw Exceptions.error("condition must not be empty");
        if (whereClause.length() > 0) whereClause.append(" AND ");
        whereClause.append(condition);
        Collections.addAll(this.params, params);
        return this;
    }

    @Override
    public Query<T> orderBy(String sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public Query<T> skip(int skip) {
        this.skip = skip;
        return this;
    }

    @Override
    public Query<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public List<T> fetch() {
        String where = whereClause.length() > 0 ? whereClause.toString() : null;
        String sql = dialect.fetchSQL(where, sort, skip, limit);
        Object[] params = fetchParams();
        return repository.fetch(sql, params);
    }

    @Override
    public int count() {
        String where = whereClause.length() > 0 ? whereClause.toString() : null;
        Object[] params = this.params.toArray(new Object[this.params.size()]);
        return repository.count(where, params);
    }

    private Object[] fetchParams() {
        if (skip != null && limit == null) throw Exceptions.error("limit must not be null if skip is not, skip={}", skip);
        if (skip == null && limit == null) return params.toArray();
        return dialect.fetchParams(params, skip, limit);
    }
}
