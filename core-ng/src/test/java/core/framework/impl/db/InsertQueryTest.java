package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class InsertQueryTest {
    @Test
    void assignedId() {
        InsertQuery<AssignedIdEntity> query = new InsertQuery<>(AssignedIdEntity.class);
        assertEquals("INSERT INTO assigned_id_entity (id, string_field, int_field, big_decimal_field, date_field) VALUES (?, ?, ?, ?, ?)", query.sql);
        assertNull(query.generatedColumn);
    }

    @Test
    void autoIncrementId() {
        InsertQuery<AutoIncrementIdEntity> query = new InsertQuery<>(AutoIncrementIdEntity.class);
        assertEquals("INSERT INTO auto_increment_id_entity (string_field, double_field, enum_field, date_time_field, zoned_date_time_field) VALUES (?, ?, ?, ?, ?)", query.sql);
        assertEquals("id", query.generatedColumn);
    }

    @Test
    void sequenceId() {
        InsertQuery<SequenceIdEntity> query = new InsertQuery<>(SequenceIdEntity.class);
        assertEquals("INSERT INTO sequence_id_entity (id, string_field, long_field) VALUES (seq.NEXTVAL, ?, ?)", query.sql);
        assertEquals("id", query.generatedColumn);
    }
}
