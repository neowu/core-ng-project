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

        assertThat(analyzer.isEfficient("Bitmap Heap Scan on orders  (cost=77.85..471.70 rows=3000 width=90)"))
            .isTrue();

        /*
HashAggregate  (cost=289230.28..296733.90 rows=168512 width=42)
  Group Key: a.field_1, a.field_2, b.field_1
  Planned Partitions: 4
  ->  Hash Left Join  (cost=12.62..249996.94 rows=425582 width=30)
        Hash Cond: ((a.field_1)::text = (b.field_1)::text)
        ->  Index Only Scan using a_idx_1 on table_a a (cost=0.43..248857.15 rows=425582 width=26)
              Index Cond: (some condition)
        ->  Hash  (cost=7.64..7.64 rows=364 width=18)
              ->  Seq Scan on table_b b (cost=0.00..7.64 rows=364 width=18)
        */
        assertThat(analyzer.isEfficient("Hash Left Join  (cost=9.55..1022.19 rows=82092 width=18)"))
            .isTrue();
        assertThat(analyzer.isEfficient("Hash  (cost=7.64..7.64 rows=364 width=18)"))
            .isTrue();
        assertThat(analyzer.isEfficient("Hash  (cost=7.64..7.64 rows=20000 width=18)"))
            .isFalse();
    }
}
