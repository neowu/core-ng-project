package core.framework.internal.db.inspector;

public interface QueryAnalyzer {
    QueryPlan explain(String sql, Object[] params);

    record QueryPlan(String plan, boolean efficient) {
    }
}
