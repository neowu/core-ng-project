public class RowMapper$AutoIncrementIdEntity implements core.framework.internal.db.RowMapper {
    private final core.framework.internal.db.DBEnumMapper enumFieldMappings = new core.framework.internal.db.DBEnumMapper(core.framework.internal.db.TestEnum.class);

    public Object map(core.framework.internal.db.ResultSetWrapper resultSet) {
        core.framework.internal.db.AutoIncrementIdEntity entity = new core.framework.internal.db.AutoIncrementIdEntity();
        entity.id = resultSet.getInt("id");
        entity.stringField = resultSet.getString("string_field");
        entity.doubleField = resultSet.getDouble("double_field");
        entity.enumField = (core.framework.internal.db.TestEnum)enumFieldMappings.getEnum(resultSet.getString("enum_field"));
        entity.dateTimeField = resultSet.getLocalDateTime("date_time_field");
        entity.zonedDateTimeField = resultSet.getZonedDateTime("zoned_date_time_field");
        return entity;
    }

}
