public class RowMapper$JSONEntity implements core.framework.internal.db.RowMapper {
    public Object map(core.framework.internal.db.ResultSetWrapper resultSet) {
        core.framework.internal.db.JSONEntity entity = new core.framework.internal.db.JSONEntity();
        entity.id = resultSet.getString("id");
        entity.jsonField = (core.framework.internal.db.JSONEntity.TestJSON) core.framework.internal.db.JSONHelper.fromJSON(resultSet.getString("json"), core.framework.internal.db.JSONEntity.TestJSON.class);
        entity.enumList = (java.util.List) core.framework.internal.db.JSONHelper.fromJSON(resultSet.getString("enum_list"), core.framework.util.Types.list(core.framework.internal.db.JSONEntity.TestJSONEnum.class));
        entity.intList = (java.util.List) core.framework.internal.db.JSONHelper.fromJSON(resultSet.getString("int_list"), core.framework.util.Types.list(java.lang.Integer.class));
        return entity;
    }

}
