package core.framework.test.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;

/**
 * @author chi
 */
@Table(name = "test_sequence_id_entity")
public class TestSequenceIdDBEntity {
    @PrimaryKey(sequence = "test_sequence")
    @Column(name = "id")
    public Long id;

    @Column(name = "int_field")
    public Integer intField;

    @Column(name = "string_field")
    public String stringField;
}
