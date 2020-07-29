public class QueryParamReader$TestQueryParamBean implements core.framework.internal.web.bean.QueryParamReader {
    public Object fromParams(java.util.Map params) {
        core.framework.internal.web.bean.TestQueryParamBean bean = new core.framework.internal.web.bean.TestQueryParamBean();
        String $zonedDateTimeField = (String)params.get("zoned_date_time_field");
        if ($zonedDateTimeField != null) {
            bean.zonedDateTimeField = core.framework.internal.web.bean.QueryParamHelper.toZonedDateTime($zonedDateTimeField);
        }
        String $dateTimeField = (String)params.get("date_time_field");
        if ($dateTimeField != null) {
            bean.dateTimeField = core.framework.internal.web.bean.QueryParamHelper.toDateTime($dateTimeField);
        }
        String $dateField = (String)params.get("date_field");
        if ($dateField != null) {
            bean.dateField = core.framework.internal.web.bean.QueryParamHelper.toDate($dateField);
        }
        String $timeField = (String)params.get("time_field");
        if ($timeField != null) {
            bean.timeField = core.framework.internal.web.bean.QueryParamHelper.toTime($timeField);
        }
        String $stringField = (String)params.get("string_field");
        if ($stringField != null) {
            bean.stringField = core.framework.internal.web.bean.QueryParamHelper.toString($stringField);
        }
        String $intField = (String)params.get("int_field");
        if ($intField != null) {
            bean.intField = core.framework.internal.web.bean.QueryParamHelper.toInt($intField);
        }
        String $longField = (String)params.get("long_field");
        if ($longField != null) {
            bean.longField = core.framework.internal.web.bean.QueryParamHelper.toLong($longField);
        }
        String $doubleField = (String)params.get("double_field");
        if ($doubleField != null) {
            bean.doubleField = core.framework.internal.web.bean.QueryParamHelper.toDouble($doubleField);
        }
        String $bigDecimalField = (String)params.get("big_decimal_field");
        if ($bigDecimalField != null) {
            bean.bigDecimalField = core.framework.internal.web.bean.QueryParamHelper.toBigDecimal($bigDecimalField);
        }
        String $booleanField = (String)params.get("boolean_field");
        if ($booleanField != null) {
            bean.booleanField = core.framework.internal.web.bean.QueryParamHelper.toBoolean($booleanField);
        }
        String $enumField = (String)params.get("enum_field");
        if ($enumField != null) {
            bean.enumField = (core.framework.internal.web.bean.TestQueryParamBean.TestEnum)core.framework.internal.web.bean.QueryParamHelper.toEnum($enumField, core.framework.internal.web.bean.TestQueryParamBean.TestEnum.class);
        }
        String $defaultValueField = (String)params.get("default_value_field");
        if ($defaultValueField != null) {
            bean.defaultValueField = core.framework.internal.web.bean.QueryParamHelper.toString($defaultValueField);
        }
        return bean;
    }

}
