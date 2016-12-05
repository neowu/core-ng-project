package core.framework.impl.web.site;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.Interceptor;
import core.framework.api.web.Invocation;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author neo
 */
public class HTTPSOnlyInterceptor implements Interceptor {
    @Override
    public Response intercept(Invocation invocation) throws Exception {
        Request request = invocation.context().request();
        if (!"https".equals(request.scheme())) {
            return Response.redirect(redirectURL(request), HTTPStatus.PERMANENT_REDIRECT);
        } else {
            Response response = invocation.proceed();
            response.header("Strict-Transport-Security", "max-age=31536000");
            return response;
        }
    }

    private String redirectURL(Request request) {
        StringBuilder builder = new StringBuilder("https://").append(request.hostName()).append(request.path());

        Map<String, String> queryParams = request.queryParams();
        if (!queryParams.isEmpty()) {
            int i = 0;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (i == 0) builder.append('?');
                else builder.append('&');
                builder.append(encode(name)).append('=').append(encode(value));
                i++;
            }
        }
        return builder.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
