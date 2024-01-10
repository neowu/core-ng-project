package core.framework.db;

public interface QueryDiagnostic {
    String sql();

    boolean noGoodIndexUsed();

    boolean noIndexUsed();
}
