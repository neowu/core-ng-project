package core.framework.impl.web.site;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author neo
 */
public class WebSecurityInterceptor implements Interceptor {    // refer to https://www.owasp.org/index.php/OWASP_Secure_Headers_Project#tab=Headers
    @Override
    public Response intercept(Invocation invocation) throws Exception {
        Request request = invocation.context().request();
        if (!"https".equals(request.scheme())) {
            return Response.redirect(redirectURL(request), HTTPStatus.MOVED_PERMANENTLY);
        } else {
            Response response = invocation.proceed();
            response.header("Strict-Transport-Security", "max-age=31536000");
            response.contentType().ifPresent(contentType -> {
                if (ContentType.TEXT_HTML.mediaType().equals(contentType.mediaType())) {
                    response.header("X-Frame-Options", "DENY");
                    response.header("X-XSS-Protection", "1; mode=block");
                }
                response.header("X-Content-Type-Options", "nosniff");
            });
            return response;
        }
    }

    private String redirectURL(Request request) {   // always assume https site is published on 443 port
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
