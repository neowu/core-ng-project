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
        var explain1 = new MySQLQueryAnalyzer.Explain();
        explain1.id = "1";
        explain1.selectType = "SIMPLE";
        explain1.table = "user";
        explain1.partitions = "p0";
        explain1.type = "ALL";
        explain1.possibleKeys = "idx_user";
        explain1.key = "idx_user";
        explain1.keyLength = 4L;
        explain1.ref = "const";
        explain1.rows = 10L;
        explain1.filtered = "100.00";
        explain1.extra = "Using where";

        var explain2 = new MySQLQueryAnalyzer.Explain();
        explain2.id = "2";
        explain2.selectType = "PRIMARY";
        explain2.table = "order";
        explain2.partitions = "p1";
        explain2.type = "range";
        explain2.possibleKeys = "idx_order";
        explain2.key = "idx_order";
        explain2.keyLength = 8L;
        explain2.ref = "user.id";
        explain2.rows = 5L;
        explain2.filtered = "50.00";
        explain2.extra = "Using index";

        var explains = List.of(explain1, explain2);
        String plan = analyzer.format(explains);

        assertThat(plan).isEqualToIgnoringWhitespace("""
            id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra
            1 | SIMPLE | user | p0 | ALL | idx_user | idx_user |4 | const |10 |100.00 | Using where
            2 | PRIMARY | order | p1 | range | idx_order | idx_order |8 | user.id |5 |50.00 | Using index""");
    }
}
