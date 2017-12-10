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
    private final String helper = QueryParamMapperHelper.class.getCanonicalName();
    private final Class<T> beanClass;

    QueryParamMapperBuilder(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public QueryParamMapper<T> build() {
        DynamicInstanceBuilder<QueryParamMapper<T>> builder = new DynamicInstanceBuilder<>(QueryParamMapper.class, QueryParamMapper.class.getCanonicalName() + "$" + beanClass.getSimpleName());
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
            if (String.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = (String)params.get(\"{}\");\n", fieldName, name);
            } else if (Integer.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toInt((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toLong((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toDouble((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toBigDecimal((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toBoolean((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toDateTime((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (LocalDate.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toDate((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (ZonedDateTime.class.equals(fieldClass)) {
                builder.indent(1).append("bean.{} = {}.toZonedDateTime((String)params.get(\"{}\"));\n", fieldName, helper, name);
            } else if (fieldClass.isEnum()) {
                builder.indent(1).append("bean.{} = ({}){}.toEnum((String)params.get(\"{}\"), {});\n", fieldName, type(fieldClass), helper, name, variable(fieldClass));
            }
        }

        builder.indent(1).append("return bean;\n");
        builder.append("}");

        return builder.build();
    }
}
