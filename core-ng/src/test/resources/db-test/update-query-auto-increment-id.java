public class UpdateQueryBuilder$AutoIncrementIdEntity implements core.framework.impl.db.UpdateQuery {
    public core.framework.impl.db.UpdateQuery.Statement update(Object value) {
        core.framework.impl.db.AutoIncrementIdEntity entity = (core.framework.impl.db.AutoIncrementIdEntity) value;
        if (entity.id == null) throw new Error("primary key must not be null, field=id");
        StringBuilder sql = new StringBuilder("UPDATE auto_increment_id_entity SET ");
        java.util.List params = new java.util.ArrayList();
        int index = 0;
        if (entity.stringField != null) {
            if (index > 0) sql.append(", ");
            sql.append("string_field = ?");
            params.add(entity.stringField);
            index++;
        }
        if (entity.doubleField != null) {
            if (index > 0) sql.append(", ");
            sql.append("double_field = ?");
            params.add(entity.doubleField);
            index++;
        }
        if (entity.enumField != null) {
            if (index > 0) sql.append(", ");
            sql.append("enum_field = ?");
            params.add(entity.enumField);
            index++;
        }
        if (entity.dateTimeField != null) {
            if (index > 0) sql.append(", ");
            sql.append("date_time_field = ?");
            params.add(entity.dateTimeField);
            index++;
        }
        if (entity.zonedDateTimeField != null) {
            if (index > 0) sql.append(", ");
            sql.append("zoned_date_time_field = ?");
            params.add(entity.zonedDateTimeField);
            index++;
        }
        sql.append(" WHERE id = ?");
        params.add(entity.id);
        return new core.framework.impl.db.UpdateQuery.Statement(sql.toString(), params.toArray());
    }

}
