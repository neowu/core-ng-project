public class RowMapper$AutoIncrementIdEntity implements core.framework.impl.db.RowMapper {
    private final core.framework.impl.db.DBEnumMapper enumFieldMappings = new core.framework.impl.db.DBEnumMapper(core.framework.impl.db.TestEnum.class);

    public Object map(core.framework.impl.db.ResultSetWrapper resultSet) {
        core.framework.impl.db.AutoIncrementIdEntity entity = new core.framework.impl.db.AutoIncrementIdEntity();
        entity.id = resultSet.getInt("id");
        entity.stringField = resultSet.getString("string_field");
        entity.doubleField = resultSet.getDouble("double_field");
        entity.enumField = (core.framework.impl.db.TestEnum)enumFieldMappings.getEnum(resultSet.getString("enum_field"));
        entity.dateTimeField = resultSet.getLocalDateTime("date_time_field");
        entity.zonedDateTimeField = resultSet.getZonedDateTime("zoned_date_time_field");
        return entity;
    }

}
