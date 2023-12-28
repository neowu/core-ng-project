package core.framework.test.db;

import core.framework.db.Column;

/**
 * @author chi
 */
public class TestDBProjection {
    @Column(name = "string_field")
    public String stringField;

    @Column(name = "sum_value")
    public Integer sum;
}
