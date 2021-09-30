public class UpdateQuery$AutoIncrementIdEntity implements core.framework.internal.db.UpdateQuery {
    public core.framework.internal.db.UpdateQuery.Statement update(Object value, boolean partial, String where, Object[] whereParams) {
        core.framework.internal.db.AutoIncrementIdEntity entity = (core.framework.internal.db.AutoIncrementIdEntity) value;
        if (entity.id == null) throw new Error("primary key must not be null, field=id");
        StringBuilder sql = new StringBuilder("UPDATE auto_increment_id_entity SET ");
        java.util.List params = new java.util.ArrayList();
        int index = 0;
        if (!partial || entity.stringField != null) {
            if (index > 0) sql.append(", ");
            sql.append("string_field = ?");
            params.add(entity.stringField);
            index++;
        }
        if (!partial || entity.doubleField != null) {
            if (index > 0) sql.append(", ");
            sql.append("double_field = ?");
            params.add(entity.doubleField);
            index++;
        }
        if (!partial || entity.enumField != null) {
            if (index > 0) sql.append(", ");
            sql.append("enum_field = ?");
            params.add(entity.enumField);
            index++;
        }
        if (!partial || entity.dateTimeField != null) {
            if (index > 0) sql.append(", ");
            sql.append("date_time_field = ?");
            params.add(entity.dateTimeField);
            index++;
        }
        if (!partial || entity.zonedDateTimeField != null) {
            if (index > 0) sql.append(", ");
            sql.append("zoned_date_time_field = ?");
            params.add(entity.zonedDateTimeField);
            index++;
        }
        sql.append(" WHERE id = ?");
        params.add(entity.id);
        if (where != null) {
            sql.append(" AND (").append(where).append(')');
            for (int i = 0; i< whereParams.length; i++) params.add(whereParams[i]);
        }
        return new core.framework.internal.db.UpdateQuery.Statement(sql.toString(), params.toArray());
    }

}
