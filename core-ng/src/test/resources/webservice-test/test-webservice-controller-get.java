public class TestWebServiceImpl$get implements core.framework.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$get(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        java.lang.Integer id = (java.lang.Integer) request.pathParam("id", java.lang.Integer.class);
        java.util.Optional response = delegate.get(id);
        return core.framework.web.Response.bean(response).status(core.framework.api.http.HTTPStatus.OK);
    }

}
