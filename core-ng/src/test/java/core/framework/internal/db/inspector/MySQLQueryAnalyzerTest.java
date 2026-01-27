package core.framework.internal.db.inspector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MySQLQueryAnalyzerTest {
    private MySQLQueryAnalyzer analyzer;

    @BeforeEach
    void createMySQLQueryAnalyzer() {
        analyzer = new MySQLQueryAnalyzer(null);
    }

    @Test
    void format() {
        var explain1 = parse("1|SIMPLE|user|p0|ALL|idx_user|idx_user |4|const|10|100.00|Using where");
        var explain2 = parse("2|PRIMARY|order|p1|range|idx_order|idx_order|8|user.id|5|50.00|Using index");

        var explains = List.of(explain1, explain2);
        String plan = analyzer.format(explains);

        assertThat(plan).isEqualToIgnoringWhitespace("""
            id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra
            1 | SIMPLE | user | p0 | ALL | idx_user | idx_user |4 | const | 10 | 100.00 | Using where
            2 | PRIMARY | order | p1 | range | idx_order | idx_order | 8 | user.id | 5 | 50.00 | Using index""");
    }

    @Test
    void isEfficient() {
        var explain = parse("1|SIMPLE|t1||ALL|idx_col1_col2|idx_col1_col2|13|const|100||Using where; Using index; Using filesort");
        assertThat(analyzer.isEfficient(explain)).isFalse();

        explain = parse("1|SIMPLE|t1||ALL|idx_col1_col2|idx_col1_col2|13|const|100||Using where; Using index");
        assertThat(analyzer.isEfficient(explain)).isTrue(); // scan less than 2000 rows

        explain = parse("1|SIMPLE|daily_individual_stats||index_merge|PRIMARY,idx_customer_id|idx_customer_id,PRIMARY|149,149||1|100|Using intersect(idx_customer_id,PRIMARY); Using where");
        assertThat(analyzer.isEfficient(explain)).isTrue();
    }

    // 1|SIMPLE|daily_individual_stats||index_merge|PRIMARY,idx_customer_id|idx_customer_id,PRIMARY|149,149||1|100|Using intersect(idx_customer_id,PRIMARY); Using where
    MySQLQueryAnalyzer.Explain parse(String value) {
        var explain = new MySQLQueryAnalyzer.Explain();
        String[] parts = value.split("\\|", -1);
        explain.id = parts[0];
        explain.selectType = parts[1];
        explain.table = parts[2];
        explain.partitions = parts[3];
        explain.type = parts[4];
        explain.possibleKeys = parts[5];
        explain.key = parts[6];
        explain.keyLength = parts[7];
        explain.ref = parts[8];
        explain.rows = parts[9].isEmpty() ? null : Long.parseLong(parts[9]);
        explain.filtered = parts[10];
        explain.extra = parts[11];
        return explain;
    }
}
