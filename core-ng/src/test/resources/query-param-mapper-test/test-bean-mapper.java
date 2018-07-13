public class QueryParamMapper$TestQueryParamBean implements core.framework.impl.web.bean.QueryParamMapper {
    public java.util.Map toParams(Object value) {
        java.util.Map params = new java.util.HashMap();
        core.framework.impl.web.bean.TestQueryParamBean bean = (core.framework.impl.web.bean.TestQueryParamBean)value;
        params.put("zoned_date_time_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.zonedDateTimeField));
        params.put("date_time_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.dateTimeField));
        params.put("date_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.dateField));
        params.put("string_field", bean.stringField);
        params.put("int_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.intField));
        params.put("long_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.longField));
        params.put("double_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.doubleField));
        params.put("big_decimal_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.bigDecimalField));
        params.put("boolean_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.booleanField));
        params.put("enum_field", core.framework.impl.web.bean.QueryParamMapperHelper.toString(bean.enumField));
        params.put("default_value_field", bean.defaultValueField);
        return params;
    }

    public Object fromParams(java.util.Map params) {
        core.framework.impl.web.bean.TestQueryParamBean bean = new core.framework.impl.web.bean.TestQueryParamBean();
        if (params.containsKey("zoned_date_time_field")) {
            bean.zonedDateTimeField = core.framework.impl.web.bean.QueryParamMapperHelper.toZonedDateTime((String)params.get("zoned_date_time_field"));
        }
        if (params.containsKey("date_time_field")) {
            bean.dateTimeField = core.framework.impl.web.bean.QueryParamMapperHelper.toDateTime((String)params.get("date_time_field"));
        }
        if (params.containsKey("date_field")) {
            bean.dateField = core.framework.impl.web.bean.QueryParamMapperHelper.toDate((String)params.get("date_field"));
        }
        if (params.containsKey("string_field")) {
            bean.stringField = (String)params.get("string_field");
        }
        if (params.containsKey("int_field")) {
            bean.intField = core.framework.impl.web.bean.QueryParamMapperHelper.toInt((String)params.get("int_field"));
        }
        if (params.containsKey("long_field")) {
            bean.longField = core.framework.impl.web.bean.QueryParamMapperHelper.toLong((String)params.get("long_field"));
        }
        if (params.containsKey("double_field")) {
            bean.doubleField = core.framework.impl.web.bean.QueryParamMapperHelper.toDouble((String)params.get("double_field"));
        }
        if (params.containsKey("big_decimal_field")) {
            bean.bigDecimalField = core.framework.impl.web.bean.QueryParamMapperHelper.toBigDecimal((String)params.get("big_decimal_field"));
        }
        if (params.containsKey("boolean_field")) {
            bean.booleanField = core.framework.impl.web.bean.QueryParamMapperHelper.toBoolean((String)params.get("boolean_field"));
        }
        if (params.containsKey("enum_field")) {
            bean.enumField = (core.framework.impl.web.bean.TestQueryParamBean.TestEnum)core.framework.impl.web.bean.QueryParamMapperHelper.toEnum((String)params.get("enum_field"), core.framework.impl.web.bean.TestQueryParamBean.TestEnum.class);
        }
        if (params.containsKey("default_value_field")) {
            bean.defaultValueField = (String)params.get("default_value_field");
        }
        return bean;
    }

}
