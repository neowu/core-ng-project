package core.framework.impl.web.bean;

import core.framework.api.web.service.QueryParam;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
class QueryParamMapperBuilder<T> {
    private final String helper = type(QueryParamMapperHelper.class);
    private final Class<T> beanClass;
    DynamicInstanceBuilder<QueryParamMapper<T>> builder;

    QueryParamMapperBuilder(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public QueryParamMapper<T> build() {
        builder = new DynamicInstanceBuilder<>(QueryParamMapper.class, QueryParamMapper.class.getCanonicalName() + "$" + beanClass.getSimpleName());
        builder.addMethod(toParamsMethod());
        builder.addMethod(toBeanMethod());
        return builder.build();
    }

    private String toParamsMethod() {
        CodeBuilder builder = new CodeBuilder().append("public java.util.Map toParams(Object value) {\n");
        builder.indent(1).append("java.util.Map params = new java.util.HashMap();\n");
        builder.indent(1).append("{} bean = ({})value;\n", type(beanClass), type(beanClass));

        for (Field field : Classes.instanceFields(beanClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String name = field.getDeclaredAnnotation(QueryParam.class).name();
            if (String.class.equals(fieldClass)) {
                builder.indent(1).append("params.put(\"{}\", bean.{});\n", name, fieldName);
            } else {
                builder.indent(1).append("params.put(\"{}\", {}.toString(bean.{}));\n", name, helper, fieldName);
            }
        }
        builder.indent(1).append("return params;\n");
        builder.append("}");

        return builder.build();
    }

    private String toBeanMethod() {
        String beanClassLiteral = type(beanClass);
        CodeBuilder builder = new CodeBuilder().append("public Object fromParams(java.util.Map params) {\n");
        builder.indent(1).append("{} bean = new {}();\n", beanClassLiteral, beanClassLiteral);

        for (Field field : Classes.instanceFields(beanClass)) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            String name = field.getDeclaredAnnotation(QueryParam.class).name();
            builder.indent(1).append("String ${} = (String)params.get(\"{}\");\n", fieldName, name);
            builder.indent(1).append("if (${} != null) {\n", fieldName);    // query param won't have null values, null means the key doesn't exist
            if (String.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toString(${});\n", fieldName, helper, fieldName);
            } else if (Integer.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toInt(${});\n", fieldName, helper, fieldName);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toLong(${});\n", fieldName, helper, fieldName);
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toDouble(${});\n", fieldName, helper, fieldName);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toBigDecimal(${});\n", fieldName, helper, fieldName);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toBoolean(${});\n", fieldName, helper, fieldName);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toDateTime(${});\n", fieldName, helper, fieldName);
            } else if (LocalDate.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toDate(${});\n", fieldName, helper, fieldName);
            } else if (ZonedDateTime.class.equals(fieldClass)) {
                builder.indent(2).append("bean.{} = {}.toZonedDateTime(${});\n", fieldName, helper, fieldName);
            } else if (fieldClass.isEnum()) {
                builder.indent(2).append("bean.{} = ({}){}.toEnum(${}, {});\n", fieldName, type(fieldClass), helper, fieldName, variable(fieldClass));
            }
            builder.indent(1).append("}\n", name);
        }

        builder.indent(1).append("return bean;\n");
        builder.append("}");

        return builder.build();
    }
}
