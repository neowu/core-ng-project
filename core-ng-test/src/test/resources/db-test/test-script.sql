CREATE INDEX idx_test_entity_int_field ON test_entity (int_field);
CREATE INDEX idx_test_entity_string_field ON test_entity (string_field);
INSERT INTO test_entity (id, int_field) VALUES ('sql-runner-test', 1);
