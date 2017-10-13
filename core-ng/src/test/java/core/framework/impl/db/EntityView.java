package core.framework.impl.db;

import core.framework.db.Column;

/**
 * @author neo
 */
public class EntityView {
    @Column(name = "id")
    public Integer id;

    @Column(name = "string_label")
    public String stringField;

    @Column(name = "enum_label")
    public TestEnum enumField;
}
