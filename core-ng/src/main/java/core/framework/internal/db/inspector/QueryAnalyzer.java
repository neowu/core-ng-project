package core.framework.internal.db.inspector;

public interface QueryAnalyzer {
    QueryPlan explain(String sql, Object[] params);
}
