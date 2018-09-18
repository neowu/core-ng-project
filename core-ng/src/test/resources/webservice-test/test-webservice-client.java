public class TestWebService$Client implements core.framework.impl.web.service.TestWebService {
    private final core.framework.impl.web.service.WebServiceClient client;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.impl.web.service.WebServiceClient.class);

    public TestWebService$Client(core.framework.impl.web.service.WebServiceClient $1) {
        this.client = $1;
    }

    public void create(java.lang.Integer param0, core.framework.impl.web.service.TestWebService.TestRequest param1) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.create(java.lang.Integer, core.framework.impl.web.service.TestWebService$TestRequest)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.impl.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = core.framework.impl.web.service.TestWebService.TestRequest.class;
        Object requestBean = param1;
        client.execute(core.framework.http.HTTPMethod.PUT, path, requestBeanClass, requestBean, void.class);
    }

    public void delete(java.lang.String param0) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.delete(java.lang.String)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.impl.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        client.execute(core.framework.http.HTTPMethod.DELETE, path, requestBeanClass, requestBean, void.class);
    }

    public java.util.Optional get(java.lang.Integer param0) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.get(java.lang.Integer)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.impl.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        return (java.util.Optional) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.util.Types.optional(core.framework.impl.web.service.TestWebService.TestResponse.class));
    }

    public core.framework.impl.web.service.TestWebService.TestResponse getEnum(java.lang.Long param0, core.framework.impl.web.service.TestWebService.TestEnum param1) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.getEnum(java.lang.Long, core.framework.impl.web.service.TestWebService$TestEnum)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        if (param1 == null) throw new Error("path param must not be null, name=enum");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.impl.web.service.PathParamHelper.toString(param0));
        builder.append("/").append(core.framework.impl.web.service.PathParamHelper.toString(param1));
        String path = builder.toString();
        Class requestBeanClass = null;
        Object requestBean = null;
        return (core.framework.impl.web.service.TestWebService.TestResponse) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.impl.web.service.TestWebService.TestResponse.class);
    }

    public void patch(java.lang.Integer param0, core.framework.impl.web.service.TestWebService.TestRequest param1) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.patch(java.lang.Integer, core.framework.impl.web.service.TestWebService$TestRequest)");
        if (param0 == null) throw new Error("path param must not be null, name=id");
        StringBuilder builder = new StringBuilder();
        builder.append("/test/").append(core.framework.impl.web.service.PathParamHelper.toString(param0));
        String path = builder.toString();
        Class requestBeanClass = core.framework.impl.web.service.TestWebService.TestRequest.class;
        Object requestBean = param1;
        client.execute(core.framework.http.HTTPMethod.PATCH, path, requestBeanClass, requestBean, void.class);
    }

    public core.framework.impl.web.service.TestWebService.TestResponse search(core.framework.impl.web.service.TestWebService.TestSearchRequest param0) {
        logger.debug("call web service, method=core.framework.impl.web.service.TestWebService.search(core.framework.impl.web.service.TestWebService$TestSearchRequest)");
        String path = "/test";
        Class requestBeanClass = core.framework.impl.web.service.TestWebService.TestSearchRequest.class;
        Object requestBean = param0;
        return (core.framework.impl.web.service.TestWebService.TestResponse) client.execute(core.framework.http.HTTPMethod.GET, path, requestBeanClass, requestBean, core.framework.impl.web.service.TestWebService.TestResponse.class);
    }

}
