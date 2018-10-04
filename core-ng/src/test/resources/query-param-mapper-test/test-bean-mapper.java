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
        String $zonedDateTimeField = (String)params.get("zoned_date_time_field");
        if ($zonedDateTimeField != null) {
            bean.zonedDateTimeField = core.framework.impl.web.bean.QueryParamMapperHelper.toZonedDateTime($zonedDateTimeField);
        }
        String $dateTimeField = (String)params.get("date_time_field");
        if ($dateTimeField != null) {
            bean.dateTimeField = core.framework.impl.web.bean.QueryParamMapperHelper.toDateTime($dateTimeField);
        }
        String $dateField = (String)params.get("date_field");
        if ($dateField != null) {
            bean.dateField = core.framework.impl.web.bean.QueryParamMapperHelper.toDate($dateField);
        }
        String $stringField = (String)params.get("string_field");
        if ($stringField != null) {
            bean.stringField = core.framework.impl.web.bean.QueryParamMapperHelper.toString($stringField);
        }
        String $intField = (String)params.get("int_field");
        if ($intField != null) {
            bean.intField = core.framework.impl.web.bean.QueryParamMapperHelper.toInt($intField);
        }
        String $longField = (String)params.get("long_field");
        if ($longField != null) {
            bean.longField = core.framework.impl.web.bean.QueryParamMapperHelper.toLong($longField);
        }
        String $doubleField = (String)params.get("double_field");
        if ($doubleField != null) {
            bean.doubleField = core.framework.impl.web.bean.QueryParamMapperHelper.toDouble($doubleField);
        }
        String $bigDecimalField = (String)params.get("big_decimal_field");
        if ($bigDecimalField != null) {
            bean.bigDecimalField = core.framework.impl.web.bean.QueryParamMapperHelper.toBigDecimal($bigDecimalField);
        }
        String $booleanField = (String)params.get("boolean_field");
        if ($booleanField != null) {
            bean.booleanField = core.framework.impl.web.bean.QueryParamMapperHelper.toBoolean($booleanField);
        }
        String $enumField = (String)params.get("enum_field");
        if ($enumField != null) {
            bean.enumField = (core.framework.impl.web.bean.TestQueryParamBean.TestEnum)core.framework.impl.web.bean.QueryParamMapperHelper.toEnum($enumField, core.framework.impl.web.bean.TestQueryParamBean.TestEnum.class);
        }
        String $defaultValueField = (String)params.get("default_value_field");
        if ($defaultValueField != null) {
            bean.defaultValueField = core.framework.impl.web.bean.QueryParamMapperHelper.toString($defaultValueField);
        }
        return bean;
    }

}
