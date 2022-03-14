public class InsertQueryParamBuilder$AssignedIdEntity implements core.framework.internal.db.InsertQueryParamBuilder {
    public Object[] params(Object value) {
        core.framework.internal.db.AssignedIdEntity entity = (core.framework.internal.db.AssignedIdEntity) value;
        if (entity.id == null) throw new Error("primary key must not be null, field=id");
        Object[] params = new Object[6];
        params[0] = entity.id;
        params[1] = entity.stringField;
        params[2] = entity.intField;
        params[3] = entity.bigDecimalField;
        params[4] = entity.dateField;
        params[5] = entity.zonedDateTimeField;
        return params;
    }

}
