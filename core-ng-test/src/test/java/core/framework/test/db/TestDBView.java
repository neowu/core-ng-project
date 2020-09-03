package core.framework.test.db;

import core.framework.db.Column;

/**
 * @author chi
 */
public class TestDBView {
    @Column(name = "id")
    public String id;

    @Column(name = "int_field")
    public Integer intField;
}
