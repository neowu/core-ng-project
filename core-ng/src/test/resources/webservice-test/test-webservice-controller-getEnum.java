public class TestWebServiceImpl$getEnum implements core.framework.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$getEnum(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Long $id = core.framework.impl.web.service.PathParamHelper.toLong(request.pathParam("id"));
        core.framework.impl.web.service.TestWebService.TestEnum $enum = (core.framework.impl.web.service.TestWebService.TestEnum)core.framework.impl.web.service.PathParamHelper.toEnum(request.pathParam("enum"), core.framework.impl.web.service.TestWebService.TestEnum.class);
        core.framework.impl.web.service.TestWebService.TestResponse response = delegate.getEnum($id, $enum);
        return core.framework.web.Response.bean(response).status(core.framework.api.http.HTTPStatus.OK);
    }

}
