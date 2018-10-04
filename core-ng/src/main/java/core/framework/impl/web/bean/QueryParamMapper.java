package core.framework.impl.web.bean;

import java.util.Map;

/**
 * @author neo
 */
interface QueryParamMapper<T> {
    Map<String, String> toParams(T bean);

    // in query param or form url encoding body, key=value, the value won't be null, can be empty string
    // by considering scenarios
    // 1. api client call service (possibly client uses old version of QueryParamBean)
    // 2. ajax from browser or js application to call service
    // by considering type safety, explicit style, default value, passing null value and validation handling
    // the best trade off is to treat empty string as null,
    // if one param is not presented in param, it will be skipped during deserialization, hence will be assigned with default value
    // only downside is with string field, if client pass "" as value, the server side will see it as null
    T fromParams(Map<String, String> params);
}
