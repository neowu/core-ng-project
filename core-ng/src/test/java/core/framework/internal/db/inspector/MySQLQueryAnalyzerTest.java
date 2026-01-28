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
        var explain1 = parse("1 | SIMPLE | user | p0 | ALL | idx_user | idx_user | 4 | const | 10 | 100.00 | Using where");
        var explain2 = parse("2 | PRIMARY | order | p1 | range | idx_order | idx_order | 8 | user.id | 5 | 50.00 | Using index");

        var explains = List.of(explain1, explain2);
        String plan = analyzer.format(explains);

        assertThat(plan).isEqualToIgnoringWhitespace("""
            id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra
            1 | SIMPLE | user | p0 | ALL | idx_user | idx_user |4 | const | 10 | 100.00 | Using where
            2 | PRIMARY | order | p1 | range | idx_order | idx_order | 8 | user.id | 5 | 50.00 | Using index""");
    }

    @Test
    void isEfficient() {
        var explain = parse("1|SIMPLE|t1||ALL|idx_col1_col2|idx_col1_col2|13|const|3000||Using where; Using index; Using filesort");
        assertThat(analyzer.isEfficient(explain)).isFalse();

        explain = parse("1|SIMPLE|daily_individual_stats||index_merge|PRIMARY,idx_customer_id|idx_customer_id,PRIMARY|149,149||1|100|Using intersect(idx_customer_id,PRIMARY); Using where");
        assertThat(analyzer.isEfficient(explain)).isTrue();

        explain = parse("1|SIMPLE|role||ALL|||||24|100|Using filesort");
        assertThat(analyzer.isEfficient(explain)).isTrue();  // scan less than 2000 rows

        explain = parse("1|SIMPLE|stat||range|PRIMARY,idx_customer_id|PRIMARY|3||7942|100.0|Using where; Using temporary; Using filesort");
        assertThat(analyzer.isEfficient(explain)).isTrue();

        // not check derived table
        // select cust.group AS group, cust.count from (SELECT group, COUNT(*) AS count FROM customer GROUP BY group) cust
        // 1|PRIMARY|<derived2>||ALL|||||196830|100|
        // 2|DERIVED|customer||index|idx_group|idx_group|82||196830|100|Using index
        explain = parse("1|PRIMARY|<derived2>||ALL|||||196830|100|");
        assertThat(analyzer.isEfficient(explain)).isTrue();

        // order by not using index with large number of returned rows
        explain = parse("1 | SIMPLE | t1 | null | ref | PRIMARY,idx_1,idx_2 | PRIMARY | 146 | const | 94212 | 100.0 | Using filesort");
        assertThat(analyzer.isEfficient(explain)).isFalse();

        // group by not using index with large number of returned rows
        // SELECT type, COUNT(1) AS count FROM customer WHERE created_time < ? GROUP BY type
        explain = parse("1 | SIMPLE | customer | null | ALL | idx_created_time | null | null | null | 203510 | 50.0 | Using where; Using temporary");
        assertThat(analyzer.isEfficient(explain)).isFalse();
    }

    // 1|SIMPLE|daily_individual_stats||index_merge|PRIMARY,idx_customer_id|idx_customer_id,PRIMARY|149,149||1|100|Using intersect(idx_customer_id,PRIMARY); Using where
    MySQLQueryAnalyzer.Explain parse(String value) {
        var explain = new MySQLQueryAnalyzer.Explain();
        String[] parts = value.split("\\|", -1);
        explain.id = parseString(parts[0]);
        explain.selectType = parseString(parts[1]);
        explain.table = parseString(parts[2]);
        explain.partitions = parseString(parts[3]);
        explain.type = parseString(parts[4]);
        explain.possibleKeys = parseString(parts[5]);
        explain.key = parseString(parts[6]);
        explain.keyLength = parseString(parts[7]);
        explain.ref = parseString(parts[8]);
        explain.rows = parts[9].isEmpty() ? null : Long.valueOf(parts[9].trim());
        explain.filtered = parseString(parts[10]);
        explain.extra = parseString(parts[11]);
        return explain;
    }

    String parseString(String value) {
        if (value.isEmpty()) return null;
        String trim = value.trim();
        if ("null".equalsIgnoreCase(trim)) return null;
        return trim;
    }
}
