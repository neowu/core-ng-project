public class InsertQueryParamBuilder$AutoIncrementIdEntity implements core.framework.internal.db.InsertQueryParamBuilder {
    public Object[] params(Object value) {
        core.framework.internal.db.AutoIncrementIdEntity entity = (core.framework.internal.db.AutoIncrementIdEntity) value;
        Object[] params = new Object[5];
        params[0] = entity.stringField;
        params[1] = entity.doubleField;
        params[2] = entity.enumField;
        params[3] = entity.dateTimeField;
        params[4] = entity.zonedDateTimeField;
        return params;
    }

}
