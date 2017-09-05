public class TestWebServiceImpl$get implements core.framework.api.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$get(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.api.web.Response execute(core.framework.api.web.Request request) throws Exception {
        java.lang.Integer id = (java.lang.Integer) request.pathParam("id", java.lang.Integer.class);
        core.framework.impl.web.service.TestWebService.TestResponse response = delegate.get(id);
        return core.framework.api.web.Response.bean(response).status(core.framework.api.http.HTTPStatus.OK);
    }

}
