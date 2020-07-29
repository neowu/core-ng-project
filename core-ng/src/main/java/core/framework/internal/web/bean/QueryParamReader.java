package core.framework.internal.web.bean;

import java.util.Map;

/**
 * @author neo
 */
public interface QueryParamReader<T> {
    // in query string or form url encoding body, key=value, the value can be empty string
    // with use cases
    //  1. api client calls web service (possibly client uses old version of QueryParamBean)
    //  2. ajax from browser or js application to call web service
    // by considering type safety, explicit style, default value, passing null value and validation handling
    // the best trade off is to treat empty string as null,
    // if one param is not present in query string, it will be skipped during deserialization, hence will be assigned with default value
    // only downside is with string field, if client pass "" as value, the server side will see it as null
    T fromParams(Map<String, String> params);
}
