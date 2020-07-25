public class TestWebService$Client implements core.framework.internal.web.service.TestWebService {
    private final core.framework.internal.web.service.WebServiceClient client;

    public TestWebService$Client(core.framework.internal.web.service.WebServiceClient $1) {
        this.client = $1;
    }

    public void create(java.lang.Integer param0, core.framework.internal.web.service.TestWebService.TestRequest param1) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.create(Integer, core.framework.internal.web.service.TestWebService$TestRequest)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = core.framework.internal.web.service.TestWebService.TestRequest.class;
        Object requestBean = param1;
        client.execute(core.framework.http.HTTPMethod.PUT, path, requestBeanClass, requestBean, void.class);
    }

    public void delete(java.lang.String param0) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.delete(String)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        client.execute(core.framework.http.HTTPMethod.DELETE, path, requestBeanClass, requestBean, void.class);
    }

    public java.util.Optional deprecated(java.lang.Integer param0) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.deprecated(Integer)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/deprecated/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        return (java.util.Optional) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.util.Types.optional(core.framework.internal.web.service.TestWebService.TestResponse.class));
    }

    public java.util.Optional get(java.lang.Integer param0) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.get(Integer)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        return (java.util.Optional) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.util.Types.optional(core.framework.internal.web.service.TestWebService.TestResponse.class));
    }

    public core.framework.internal.web.service.TestWebService.TestResponse getEnum(java.lang.Long param0, core.framework.internal.web.service.TestWebService.TestEnum param1) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.getEnum(Long, core.framework.internal.web.service.TestWebService$TestEnum)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        if (param1 == null) throw new Error("path param must not be null, name=enum");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        builder.append("/").append(core.framework.internal.web.service.PathParamHelper.toString(param1));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        return (core.framework.internal.web.service.TestWebService.TestResponse) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.internal.web.service.TestWebService.TestResponse.class);
    }

    public void patch(java.lang.Integer param0, core.framework.internal.web.service.TestWebService.TestRequest param1) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.patch(Integer, core.framework.internal.web.service.TestWebService$TestRequest)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.internal.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = core.framework.internal.web.service.TestWebService.TestRequest.class;
        Object requestBean = param1;
        client.execute(core.framework.http.HTTPMethod.PATCH, path, requestBeanClass, requestBean, void.class);
    }

    public core.framework.internal.web.service.TestWebService.TestResponse search(core.framework.internal.web.service.TestWebService.TestSearchRequest param0) {
        client.logCallWebService("core.framework.internal.web.service.TestWebService.search(core.framework.internal.web.service.TestWebService$TestSearchRequest)");
        String path = "/test";
        Class requestBeanClass = core.framework.internal.web.service.TestWebService.TestSearchRequest.class;
        Object requestBean = param0;
        return (core.framework.internal.web.service.TestWebService.TestResponse) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.internal.web.service.TestWebService.TestResponse.class);
    }

    public void intercept(core.framework.web.service.WebServiceClientInterceptor interceptor) {
        client.intercept(interceptor);
    }

}
