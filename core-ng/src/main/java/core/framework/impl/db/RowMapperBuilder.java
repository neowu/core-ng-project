package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.util.Lists;
import core.framework.api.util.Strings;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author neo
 */
final class RowMapperBuilder<T> {
    private final Class<T> entityClass;
    private final EnumDBMapper enumDBMapper;

    RowMapperBuilder(Class<T> entityClass, EnumDBMapper enumDBMapper) {
        this.entityClass = entityClass;
        this.enumDBMapper = enumDBMapper;
    }

    RowMapper<T> build() {
        List<String> enumMapperFields = Lists.newArrayList();

        String entityClassName = entityClass.getCanonicalName();

        CodeBuilder builder = new CodeBuilder().append("public Object map({} resultSet) {\n", ResultSetWrapper.class.getCanonicalName());
        builder.indent(1).append("{} entity = new {}();\n", entityClassName, entityClassName);

        for (Field field : Classes.instanceFields(entityClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String column = field.getAnnotation(Column.class).name();
            if (Integer.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getInt(\"{}\");\n", fieldName, column);
            } else if (String.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getString(\"{}\");\n", fieldName, column);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBoolean(\"{}\");\n", fieldName, column);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLong(\"{}\");\n", fieldName, column);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getLocalDateTime(\"{}\");\n", fieldName, column);
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                registerEnumClass(fieldClass);
                enumMapperFields.add(Strings.format("private final {} {}Mappings = new {}({}.class);", DBEnumMapper.class.getCanonicalName(), fieldName, DBEnumMapper.class.getCanonicalName(), fieldClass.getCanonicalName()));
                builder.indent(1).append("entity.{} = ({}){}Mappings.getEnum(resultSet.getString(\"{}\"));\n", fieldName, fieldClass.getCanonicalName(), fieldName, column);
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getDouble(\"{}\");\n", fieldName, column);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(1).append("entity.{} = resultSet.getBigDecimal(\"{}\");\n", fieldName, column);
            }
        }
        builder.indent(1).append("return entity;\n");
        builder.append("}");

        DynamicInstanceBuilder<RowMapper<T>> instanceBuilder = new DynamicInstanceBuilder<>(RowMapper.class, RowMapper.class.getCanonicalName() + "$" + entityClass.getSimpleName());
        enumMapperFields.forEach(instanceBuilder::addField);
        instanceBuilder.addMethod(builder.build());
        return instanceBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void registerEnumClass(Class<?> fieldClass) {
        Class<? extends Enum> enumClass = (Class<? extends Enum>) fieldClass;
        enumDBMapper.registerEnumClass(enumClass);
    }
}
