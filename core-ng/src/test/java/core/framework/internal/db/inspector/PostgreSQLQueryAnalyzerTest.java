package core.framework.internal.db.inspector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSQLQueryAnalyzerTest {
    private PostgreSQLQueryAnalyzer analyzer;

    @BeforeEach
    void createPostgreSQLQueryAnalyzer() {
        analyzer = new PostgreSQLQueryAnalyzer(null);
    }

    @Test
    void isEfficient() {
        assertThat(analyzer.isEfficient("Seq Scan on orders  (cost=0.00..657.94 rows=2001 width=96)"))
            .isFalse();
        assertThat(analyzer.isEfficient("Seq Scan on orders  (cost=0.00..716.33 rows=19 width=90)"))
            .isTrue();

        assertThat(analyzer.isEfficient("Bitmap Heap Scan on orders  (cost=77.85..471.70 rows=24 width=90)"))
            .isTrue();
    }
}
