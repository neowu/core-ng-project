package core.framework.internal.db;

import core.framework.db.DBEnumValue;

/**
 * @author neo
 */
public enum TestEnum {
    @DBEnumValue("DB_V1")
    V1,
    @DBEnumValue("DB_V2")
    V2
}
