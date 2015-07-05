package core.framework.impl.db;

import core.framework.api.db.Column;

/**
 * @author neo
 */
public class EntityView {
    @Column(name = "id")
    public Integer id;

    @Column(name = "string_field")
    public String stringField;

    @Column(name = "enum_field")
    public TestEnum enumField;
}
