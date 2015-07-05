package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Lists;
import core.framework.api.util.Types;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.ResponseImpl;
import core.framework.api.web.service.PathParam;
import core.framework.impl.web.response.BeanBody;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author neo
 */
public class ServiceControllerBuilderTest {
    public static class TestWebServiceImpl implements TestWebService {
        @Override
        public TestResponse get(Integer id) {
            Assert.assertEquals(1, (int) id);

            TestResponse response = new TestResponse();
            response.intField = 2;
            return response;
        }

        @Override
        public void create(@PathParam("id") Integer id, TestRequest request) {
            Assert.assertEquals(1, (int) id);
            Assert.assertEquals("value", request.stringField);
        }

        @Override
        public void delete(@PathParam("id") String id) {

        }

        @Override
        public List<TestResponse> batch(List<TestRequest> requests) {
            Assert.assertEquals(1, requests.size());
            return Lists.newArrayList();
        }
    }

    TestWebServiceImpl serviceImpl = new TestWebServiceImpl();

    @Test
    public void get() throws Exception {
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.pathParam("id", Integer.class)).thenReturn(1);

        Controller controller = new ServiceControllerBuilder<>(TestWebService.class,
            serviceImpl,
            TestWebService.class.getDeclaredMethod("get", Integer.class))
            .build();
        Response response = controller.execute(request);
        Assert.assertEquals(HTTPStatus.OK, response.status());
        Assert.assertEquals(2, (int) ((TestWebService.TestResponse) ((BeanBody) ((ResponseImpl) response).body).bean).intField);
    }

    @Test
    public void create() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        Request request = Mockito.mock(Request.class);
        Mockito.when(request.pathParam("id", Integer.class)).thenReturn(1);
        Mockito.when(request.bean(TestWebService.TestRequest.class)).thenReturn(requestBean);

        Controller controller = new ServiceControllerBuilder<>(TestWebService.class,
            serviceImpl,
            TestWebService.class.getDeclaredMethod("create", Integer.class, TestWebService.TestRequest.class))
            .build();
        Response response = controller.execute(request);
        Assert.assertEquals(HTTPStatus.CREATED, response.status());
    }

    @Test
    public void batch() throws Exception {
        TestWebService.TestRequest requestBean = new TestWebService.TestRequest();
        requestBean.stringField = "value";

        Request request = Mockito.mock(Request.class);
        Mockito.when(request.bean(Types.list(TestWebService.TestRequest.class))).thenReturn(Lists.newArrayList(requestBean));

        Controller controller = new ServiceControllerBuilder<>(TestWebService.class,
            serviceImpl,
            TestWebService.class.getDeclaredMethod("batch", List.class))
            .build();
        Response response = controller.execute(request);
        Assert.assertEquals(HTTPStatus.OK, response.status());
    }
}