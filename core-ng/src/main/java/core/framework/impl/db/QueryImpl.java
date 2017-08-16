package core.framework.impl.db;

import core.framework.api.db.Query;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import core.framework.impl.db.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author neo
 */
public class QueryImpl<T> implements Query<T> {
    private final Logger logger = LoggerFactory.getLogger(QueryImpl.class);

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
        StopWatch watch = new StopWatch();
        String sql = dialect.fetchSQL(whereClause, sort, skip, limit);
        Object[] params = fetchParams();
        try {
            List<T> results = repository.database.operation.select(sql, repository.rowMapper, params);
            repository.checkTooManyRowsReturned(results.size());
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("fetch, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            repository.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public int count() {
        StopWatch watch = new StopWatch();
        String sql = repository.selectQuery.countSQL(whereClause.length() > 0 ? whereClause.toString() : null);
        Object[] params = this.params.toArray(new Object[this.params.size()]);
        try {
            return repository.database.operation.selectOne(sql, new RowMapper.IntegerRowMapper(), params).get();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("count, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            repository.checkSlowOperation(elapsedTime);
        }
    }

    private Object[] fetchParams() {
        if (skip != null && limit == null) throw Exceptions.error("limit must not be null if skip is not, skip={}", skip);
        if (skip == null && limit == null) return params.toArray(new Object[params.size()]);
        return dialect.fetchParams(params, skip, limit);
    }
}
