package core.framework.api.db;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public final class Query {
    public static Query query(String sql, Object... params) {
        Query query = new Query(sql);
        for (Object param : params) {
            query.addParam(param);
        }
        return query;
    }

    private final StringBuilder statement;
    public final List<Object> params = Lists.newArrayList();

    public Query(String statement) {
        this.statement = new StringBuilder(statement);
    }

    public Query appendStatement(String statement) {
        this.statement.append(statement);
        return this;
    }

    public String statement() {
        return statement.toString();
    }

    public Query addParam(Object param) {
        params.add(param);
        return this;
    }
}
