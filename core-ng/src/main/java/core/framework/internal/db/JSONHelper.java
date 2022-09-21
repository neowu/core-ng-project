package core.framework.internal.db;

import core.framework.json.JSON;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class JSONHelper {
    // support null, doesn't need validator, validation will be done by EntityClass validator (validator checks all cascaded fields)

    // used by core.framework.internal.db.InsertQueryBuilder
    // used by core.framework.internal.db.UpdateQueryBuilder
    public static String toJSON(Object bean) {
        if (bean == null) return null;
        return JSON.toJSON(bean);
    }

    // used by core.framework.internal.db.RowMapperBuilder
    public static Object fromJSON(String json, Type type) {
        if (json == null) return null;
        return JSON.fromJSON(type, json);
    }
}
