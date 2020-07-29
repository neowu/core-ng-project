public class QueryParamWriter$TestQueryParamBean implements core.framework.internal.web.bean.QueryParamWriter {
    public java.util.Map toParams(Object value) {
        java.util.Map params = new java.util.HashMap();
        core.framework.internal.web.bean.TestQueryParamBean bean = (core.framework.internal.web.bean.TestQueryParamBean)value;
        params.put("zoned_date_time_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.zonedDateTimeField));
        params.put("date_time_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.dateTimeField));
        params.put("date_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.dateField));
        params.put("time_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.timeField));
        params.put("string_field", bean.stringField);
        params.put("int_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.intField));
        params.put("long_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.longField));
        params.put("double_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.doubleField));
        params.put("big_decimal_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.bigDecimalField));
        params.put("boolean_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.booleanField));
        params.put("enum_field", core.framework.internal.web.bean.QueryParamHelper.toString(bean.enumField));
        params.put("default_value_field", bean.defaultValueField);
        return params;
    }

}
