package core.framework.internal.db.inspector;

import core.framework.internal.db.inspector.QueryAnalyzer.QueryPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryInspectorTest {
    @Mock
    QueryAnalyzer analyzer;
    private QueryInspector inspector;

    @BeforeEach
    void createQueryInspector() {
        inspector = new QueryInspector(analyzer);
    }

    @Test
    void validateSQLWithDDL() {
        inspector.validateSQL("""
            CREATE TYPE status AS ENUM ('ACTIVE', 'INACTIVE')""");
    }

    @Test
    void validateSQLWithAsterisk() {
        inspector.validateSQL("select column * 10 from table");
        inspector.validateSQL("select 3*5, 4*2 from table");
        inspector.validateSQL("select 3 * ? from table");

        assertThatThrownBy(() -> inspector.validateSQL("select * from table")).isInstanceOf(Error.class);
        assertThatThrownBy(() -> inspector.validateSQL("select * from")).isInstanceOf(Error.class);
        assertThatThrownBy(() -> inspector.validateSQL("select t.* , t.column from table t")).isInstanceOf(Error.class);
        assertThatThrownBy(() -> inspector.validateSQL("select 3*4, * from table")).isInstanceOf(Error.class);
        assertThatThrownBy(() -> inspector.validateSQL("select *")).isInstanceOf(Error.class);
    }

    @Test
    void explain() {
        String sql = "select column from table";
        Object[] params = new Object[0];
        when(analyzer.explain(anyString(), any())).thenReturn(new QueryPlan("plan", false));

        inspector.explain(sql, params, false);
        inspector.explain(sql, params, false);

        verify(analyzer, times(1)).explain(sql, params);
    }

    @Test
    void forceExplain() {
        String sql = "select column from table";
        Object[] params = new Object[0];
        when(analyzer.explain(anyString(), any())).thenReturn(new QueryPlan("plan", false));

        inspector.explain(sql, params, true);
        inspector.explain(sql, params, true);

        verify(analyzer, times(2)).explain(sql, params);
    }
}
