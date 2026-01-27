package core.framework.internal.db.inspector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryInspectorTest {
    private QueryInspector inspector;

    @BeforeEach
    void createQueryInspector() {
        inspector = new QueryInspector(null);
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
}
