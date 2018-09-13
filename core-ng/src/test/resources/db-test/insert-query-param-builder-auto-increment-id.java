public class InsertQuery$AutoIncrementIdEntity$ParamBuilder implements java.util.function.Function {
    public Object apply(Object value) {
        core.framework.impl.db.AutoIncrementIdEntity entity = (core.framework.impl.db.AutoIncrementIdEntity) value;
        Object[] params = new Object[5];
        params[0] = entity.stringField;
        params[1] = entity.doubleField;
        params[2] = entity.enumField;
        params[3] = entity.dateTimeField;
        params[4] = entity.zonedDateTimeField;
        return params;
    }

}
